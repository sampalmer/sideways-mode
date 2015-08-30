package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetBooleanContract extends GetValueContract<Boolean> {
	@Override
	public String methodName() {
		return "getBoolean";
	}

	@Override
	public Boolean process(SharedPreferences sharedPreferences, GetValueArgs<Boolean> args) {
		return sharedPreferences.getBoolean(args.key, args.defValue);
	}
}
