package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.TwoStatePreference;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class ForcedAppsFragment extends PreferenceFragment {

	private Settings.Setting<Set<String>> appsSettings;
	private Preference appsPreference;

	private TwoStatePreference accessibilityServicePreference;
	private AccessibilityServiceEnabledStateChecker accessibilityServiceChecker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_overridden_apps);

		appsSettings = ApplicationSettings.remote().forcedOrientationAppPackages();
		appsPreference = findPreference(appsSettings.getKey());

		accessibilityServiceChecker = new AccessibilityServiceEnabledStateChecker();
		accessibilityServicePreference = (TwoStatePreference)findPreference("forcedOrientationAccessibilityService");
		accessibilityServicePreference.setOnPreferenceChangeListener(onCheckedChangeListener);
		updateAccessibilityServiceState();
	}

	@Override
	public void onStart() {
		super.onStart();

		appsSettings.subscribe(appsChanged);
		updateAppsSummary();

		updateAccessibilityServiceState(); //Maybe the accessibility service was enabled or disabled since this activity was last visible.
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

	private final Preference.OnPreferenceChangeListener onCheckedChangeListener = new Preference.OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			//TODO: Instruct the user about what to do before sending them to the accessibility settings
			//TODO: Also warn the user that they might not be able to enable the accessibility service if a screen dimmer or other screen orientation controlling app is present
			//Note that it's possible that Samsung's app optimization feature might disable the accessibility service, although this might not occur since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272)
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

			try {
				startActivity(intent);
			}
			catch (ActivityNotFoundException ex) {
				//TODO: Apparently the accessibility service management activity isn't guaranteed to actually exist. Decide what to do here.
				throw ex;
			}

			return false;
		}

	};

	private void updateAccessibilityServiceState() {
		accessibilityServicePreference.setChecked(accessibilityServiceChecker.isEnabled());
	}

}
