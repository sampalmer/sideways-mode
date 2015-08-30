package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetFloatContract extends GetValueContract<Float> {
	@Override
	public String methodName() {
		return "getFloat";
	}

	@Override
	public Float process(SharedPreferences sharedPreferences, GetValueArgs<Float> args) {
		return sharedPreferences.getFloat(args.key, args.defValue);
	}
}
