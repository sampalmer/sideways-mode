package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

import java.util.Collections;
import java.util.Set;


/**
 * @implNote Using compat preferences since built-in ones are deprecated in Android P
 */
public class IncompatibleAppsFragment extends PreferenceFragmentCompat {

    private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

    private SharedPreferences prefs;
    private Preference appsPreference;
    private boolean isStarted;
    
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_incompatible_apps);
        appsPreference = findPreference(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES);

        //noinspection ConstantConditions
        IncompatibleAppsPermissionFragment permissionFragment = (IncompatibleAppsPermissionFragment) getFragmentManager().findFragmentByTag(TAG_PERMISSIONS_FRAGMENT);
        if (permissionFragment == null) {
            permissionFragment = new IncompatibleAppsPermissionFragment();

            getFragmentManager()
                    .beginTransaction()
                    .add(permissionFragment, TAG_PERMISSIONS_FRAGMENT)
                    .commit();
        }

        // This needs to be done before the activity is even created so the tip-view can be hidden without flickering.
        permissionFragment.initialise(getPreferenceScreen());
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        prefs = ApplicationSettings.getSharedPreferences(getActivity());
        
        appsPreference.setOnPreferenceClickListener(preference -> {
            RecyclerView prefsList = IncompatibleAppsFragment.super.getListView();
            NavController navController = Navigation.findNavController(prefsList);
            navController.navigate(R.id.action_incompatibleAppsFragment_to_incompatibleAppsChooserFragment);
            return true;
        });
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
            if (isStarted && s.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
                updateAppsSummary();
        }
    };

    private void updateAppsSummary() {
        Set<String> incompatibleApps = prefs.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
        assert incompatibleApps != null;
        final int quantity = incompatibleApps.size();
        final String quantityString = getResources().getQuantityString(R.plurals.preference_incompatible_apps_summary_enabled, quantity, quantity);
        appsPreference.setSummary(quantityString);
    }

}
