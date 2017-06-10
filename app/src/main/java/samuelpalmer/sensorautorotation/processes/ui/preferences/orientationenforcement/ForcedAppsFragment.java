package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class ForcedAppsFragment extends PreferenceFragment {

	private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

	private Settings.Setting<Set<String>> apps;
	private Preference appsPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_overridden_apps);
		
		apps = ApplicationSettings.instance().forcedOrientationAppPackages();
		appsPreference = findPreference(apps.getKey());

		ForcedAppsPermissionFragment permissionFragment = (ForcedAppsPermissionFragment) getFragmentManager().findFragmentByTag(TAG_PERMISSIONS_FRAGMENT);
		if (permissionFragment == null) {
		    permissionFragment = new ForcedAppsPermissionFragment();

			getFragmentManager()
					.beginTransaction()
					.add(permissionFragment, TAG_PERMISSIONS_FRAGMENT)
					.commit();
		}

		permissionFragment.initialise(getPreferenceScreen());
	}

	@Override
	public void onStart() {
		super.onStart();

		apps.subscribe(appsChanged);
		updateAppsSummary();
	}

	@Override
	public void onStop() {
		super.onStop();
		apps.unsubscribe(appsChanged);
	}

	private final EventHandler appsChanged = new EventHandler() {
		@Override
		public void update() {
			updateAppsSummary();
		}
	};

	private void updateAppsSummary() {
		final int quantity = apps.get().size();
		final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
		appsPreference.setSummary(quantityString);
	}

}
