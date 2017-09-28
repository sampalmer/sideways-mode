package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import samuelpalmer.common.contentprovidersharedpreferences.ContentProviderSharedPreferences;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;


public abstract class ProcessIndependentSettings extends Settings {
	
	protected static SharedPreferences makePreferences(String sharedPreferencesName) {
		if (CurrentProcess.isServiceProcess())
			// For use in the service process for improved performance.
			return CurrentProcess.context().getSharedPreferences(sharedPreferencesName, 0);
		else
			// For use outside the service process for concurrency safety.
			return new ContentProviderSharedPreferences(CurrentProcess.context(), StringResource.get(R.string.preferences_provider_authority).toString(), sharedPreferencesName);
	}
	
	protected ProcessIndependentSettings(SharedPreferences prefs) {
		super(prefs);
	}
	
}
