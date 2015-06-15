package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

abstract class Prefs {
	public static SharedPreferences prefs() {
		//Using multi-process mode just in case it increases thread safety.
		return Process.context().getSharedPreferences(ApplicationSettings.sharedPreferencesName(), Context.MODE_MULTI_PROCESS);
	}
}
