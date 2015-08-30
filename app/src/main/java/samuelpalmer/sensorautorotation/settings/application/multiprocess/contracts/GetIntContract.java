package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetIntContract extends GetValueContract<Integer> {
	@Override
	public String methodName() {
		return "getInt";
	}

	@Override
	public Integer process(SharedPreferences sharedPreferences, GetValueArgs<Integer> args) {
		return sharedPreferences.getInt(args.key, args.defValue);
	}
}
