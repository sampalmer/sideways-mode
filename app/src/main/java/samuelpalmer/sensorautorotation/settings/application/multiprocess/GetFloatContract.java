package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.SharedPreferences;

class GetFloatContract extends GetValueContract<Float> {
	@Override
	public String methodName() {
		return "getFloat";
	}

	@Override
	public Float process(SharedPreferences sharedPreferences, GetValueArgs<Float> args) {
		return sharedPreferences.getFloat(args.key, args.defValue);
	}
}
