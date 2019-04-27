package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;


/**
 * @implNote Using compat preferences since built-in ones are deprecated in Android P
 */
public class IncompatibleAppsFragment extends PreferenceFragmentCompat {

    private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

    private Preference appsPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IncompatibleAppsViewModel viewModel = ViewModelProviders.of(this).get(IncompatibleAppsViewModel.class);

        viewModel.getIncompatibleAppCount().observe(this, quantity -> {
            final String quantityString = getResources().getQuantityString(R.plurals.preference_incompatible_apps_summary_enabled, quantity, quantity);
            appsPreference.setSummary(quantityString);
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_incompatible_apps);

        appsPreference = findPreference(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES);

        Preference description = findPreference("incompatibleAppsDetails");
        String newSummary = getString(R.string.preference_incompatible_apps_details, getText(R.string.app_name_full));
        description.setSummary(newSummary);

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

        appsPreference.setOnPreferenceClickListener(preference -> {
            RecyclerView prefsList = IncompatibleAppsFragment.super.getListView();
            NavController navController = Navigation.findNavController(prefsList);
            navController.navigate(R.id.action_incompatibleAppsFragment_to_incompatibleAppsChooserFragment);
            return true;
        });
    }

}
