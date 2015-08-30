package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.net.Uri;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

class Remote {

	private final String preferencesName;

	public Remote(String preferencesName) {
		this.preferencesName = preferencesName;
	}

	public <TArgs, TResult> TResult process(Contract<TArgs, TResult> contract, TArgs args) {
		Bundle serialisedResult = Process.context().getContentResolver().call(
			new Uri.Builder()
				.scheme("content")
				.authority(StringResource.get(R.string.content_provider_authority).toString())
				.build(),
			contract.methodName(),
			preferencesName,
			contract.serialiseArgs(args)
		);

		if (serialisedResult == null)
			throw new RuntimeException("Got back null. The content provider probably crashed.");

		return contract.deserialiseResult(serialisedResult);
	}

}
