package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.SharedPreferences;

class GetIntContract extends GetValueContract<Integer> {
	@Override
	public String methodName() {
		return "getInt";
	}

	@Override
	public Integer process(SharedPreferences sharedPreferences, GetValueArgs<Integer> args) {
		return sharedPreferences.getInt(args.key, args.defValue);
	}
}
