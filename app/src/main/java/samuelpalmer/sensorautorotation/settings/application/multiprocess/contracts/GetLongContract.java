package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetLongContract extends GetValueContract<Long> {
	@Override
	public String methodName() {
		return "getLong";
	}

	@Override
	public Long process(SharedPreferences sharedPreferences, GetValueArgs<Long> args) {
		return sharedPreferences.getLong(args.key, args.defValue);
	}
}
