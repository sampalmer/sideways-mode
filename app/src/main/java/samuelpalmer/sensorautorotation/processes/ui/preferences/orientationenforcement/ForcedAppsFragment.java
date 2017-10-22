package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import java.util.Collections;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;


public class ForcedAppsFragment extends PreferenceFragment {

	private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

	private SharedPreferences prefs;
	private Preference appsPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_overridden_apps);
		
		prefs = ApplicationSettings.getSharedPreferences(CurrentProcess.context());
		appsPreference = findPreference(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES);

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

		prefs.registerOnSharedPreferenceChangeListener(appsChanged);
		updateAppsSummary();
	}

	@Override
	public void onStop() {
		super.onStop();
		prefs.unregisterOnSharedPreferenceChangeListener(appsChanged);
	}

	private final SharedPreferences.OnSharedPreferenceChangeListener appsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (s.equals(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES))
				updateAppsSummary();
		}
	};

	private void updateAppsSummary() {
		final int quantity = prefs.getStringSet(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.<String>emptySet()).size();
		final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
		appsPreference.setSummary(quantityString);
	}

}
