package samuelpalmer.sensorautorotation.settings.application.multiprocess.contracts;

import android.content.SharedPreferences;

import java.util.Set;

import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueArgs;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.GetValueContract;

public class GetStringSetContract extends GetValueContract<Set<String>> {
	@Override
	public String methodName() {
		return "getStringSet";
	}

	@Override
	public Set<String> process(SharedPreferences sharedPreferences, GetValueArgs<Set<String>> args) {
		return sharedPreferences.getStringSet(args.key, args.defValue);
	}
}
