package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.common.settings.application.SettingsInitialiser;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class MainSettingsFragment extends PreferenceFragment {

	private Preference forcedAppsPreference;
	private AccessibilityServiceEnabledStateMonitor accessibilityServiceEnabledStateMonitor;
	private Settings.Setting<Set<String>> forcedApps;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		//TODO: Apply this to the other settings fragments as well
		new SettingsInitialiser(ApplicationSettings.remote()).ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);

		//TODO: move all of this preference summary updating stuff into its own class
		forcedApps = ApplicationSettings.remote().forcedOrientationAppPackages();
		forcedAppsPreference = findPreference(forcedApps.getKey());
		accessibilityServiceEnabledStateMonitor = new AccessibilityServiceEnabledStateMonitor();
	}

	@Override
	public void onStart() {
		super.onStart();

		accessibilityServiceEnabledStateMonitor.subscribe(forcedAppsChanged);
		forcedApps.subscribe(forcedAppsChanged);
		updateForcedApps();
	}

	@Override
	public void onStop() {
		super.onStop();

		forcedApps.unsubscribe(forcedAppsChanged);
		accessibilityServiceEnabledStateMonitor.unsubscribe(forcedAppsChanged);
	}

	private final EventHandler forcedAppsChanged = new EventHandler() {
		@Override
		public void update() {
			updateForcedApps();
		}
	};

	private void updateForcedApps() {
		final int quantity = forcedApps.get().size();

		if (quantity > 0 && !(accessibilityServiceEnabledStateMonitor.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(getActivity())))
			forcedAppsPreference.setSummary(R.string.permission_needed_summary);
		else {
			final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
			forcedAppsPreference.setSummary(quantityString);
		}
	}

}
