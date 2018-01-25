package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import java.util.Collections;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;


public class ForcedAppsFragment extends PreferenceFragment {

	private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

	private SharedPreferences prefs;
	private Preference appsPreference;
	private boolean isStarted;
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences_overridden_apps);
		
		prefs = ApplicationSettings.getSharedPreferences(getActivity());
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
		
		isStarted = true;
	}

	@Override
	public void onStop() {
		super.onStop();
		
		isStarted = false;
		
		prefs.unregisterOnSharedPreferenceChangeListener(appsChanged);
	}

	private final SharedPreferences.OnSharedPreferenceChangeListener appsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (isStarted && s.equals(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES))
				updateAppsSummary();
		}
	};

	private void updateAppsSummary() {
		final int quantity = prefs.getStringSet(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.emptySet()).size();
		final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
		appsPreference.setSummary(quantityString);
	}

}
