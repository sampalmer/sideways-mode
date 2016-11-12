package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class ForcedAppsFragment extends PreferenceFragment {

	private Settings.Setting<Set<String>> appsSettings;
	private Preference appsPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_overridden_apps);
		appsSettings = ApplicationSettings.remote().forcedOrientationAppPackages();
		appsPreference = findPreference(appsSettings.getKey());
	}

	@Override
	public void onStart() {
		super.onStart();
		appsSettings.subscribe(appsChanged);
		updateAppsSummary();
	}

	@Override
	public void onStop() {
		super.onStop();
		appsSettings.unsubscribe(appsChanged);
	}

	private final EventHandler appsChanged = new EventHandler() {
		@Override
		public void update() {
			updateAppsSummary();
		}
	};

	private void updateAppsSummary() {
		final int quantity = appsSettings.get().size();
		final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
		appsPreference.setSummary(quantityString);
	}


}
