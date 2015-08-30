package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetStringContract extends GetValueContract<String> {
	@Override
	public String methodName() {
		return "getString";
	}

	@Override
	public String process(SharedPreferences sharedPreferences, GetValueArgs<String> args) {
		return sharedPreferences.getString(args.key, args.defValue);
	}
}
