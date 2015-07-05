package samuelpalmer.sensorautorotation.settings.application.multiprocess;

import android.content.Context;
import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.Process;

class UnderlyingSharedPreferencesFactory {

	private final String preferencesName;

	public UnderlyingSharedPreferencesFactory(String sharedPreferencesName) {
		preferencesName = sharedPreferencesName;
	}

	/**
	 * Makes a new instance on each call since apparently that circumvents process-wide caching.
	 *
	 * See https://code.google.com/p/android/issues/detail?id=66625
	 */
	public SharedPreferences preferences() {
		return Process.context().getSharedPreferences(
				preferencesName,
				Context.MODE_MULTI_PROCESS //This actually just causes the preferences to be reloaded from storage in this method call
		);
	}

}
