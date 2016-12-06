package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * UI proxy to settings stored in background process.
 */
public class RemotePreferencesActivity extends UiActivity {

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		if (name.equals(ApplicationSettings.sharedPreferencesName()))
			return ApplicationSettings.remote().sharedPreferences;
		else
			return super.getSharedPreferences(name, mode);
	}

}
