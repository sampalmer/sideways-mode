package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.os.Bundle;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Top-level preferences screen for the incompatible apps feature.
 */
public class IncompatibleAppsFragment extends PreferenceFragmentCompat {

    private static final String TAG_PERMISSIONS_FRAGMENT = "permissions";

    private IncompatibleAppsPermissionViewModel viewModel;
    private IncompatibleAppsPermissionFragment permissionFragment;
    private PermissionTipViewPreference tipView;
    private Preference appsPreference;
    private boolean preferencesCreated;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Preparing the view-model before onCreate() so that it's ready for use as soon as the preferences are created.
        viewModel = ViewModelProviders.of(this).get(IncompatibleAppsPermissionViewModel.class);
        viewModel.getShouldShowTip().observe(this, shouldShowTip -> updateTipVisibility());
        updateTipVisibility();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_incompatible_apps);

        tipView = (PermissionTipViewPreference) findPreference("incompatibleAppsPermission");
        tipView.setOnPreferenceClickListener(preference -> {
            permissionFragment.grantPermissions();
            return true;
        });

        appsPreference = findPreference(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES);
        appsPreference.setOnPreferenceClickListener(preference -> {
            RecyclerView prefsList = IncompatibleAppsFragment.super.getListView();
            NavController navController = Navigation.findNavController(prefsList);
            navController.navigate(R.id.action_incompatibleAppsFragment_to_incompatibleAppsChooserFragment);
            return true;
        });

        Preference description = findPreference("incompatibleAppsDetails");
        String newSummary = getString(R.string.preference_incompatible_apps_details, getText(R.string.app_name_full));
        description.setSummary(newSummary);

        preferencesCreated = true;

        // This needs to be done before the activity is even created so the tip-view can be hidden without flickering.
        updateTipVisibility();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //noinspection ConstantConditions
        permissionFragment = (IncompatibleAppsPermissionFragment) getFragmentManager().findFragmentByTag(TAG_PERMISSIONS_FRAGMENT);
        if (permissionFragment == null) {
            permissionFragment = new IncompatibleAppsPermissionFragment();

            getFragmentManager()
                    .beginTransaction()
                    .add(permissionFragment, TAG_PERMISSIONS_FRAGMENT)
                    .commit();
        }

        IncompatibleAppsViewModel viewModel = ViewModelProviders.of(this).get(IncompatibleAppsViewModel.class);

        viewModel.getIncompatibleAppCount().observe(this, quantity -> {
            final String quantityString = getResources().getQuantityString(R.plurals.preference_incompatible_apps_summary_enabled, quantity, quantity);
            appsPreference.setSummary(quantityString);
        });
    }

    private void updateTipVisibility() {
        Boolean shouldShowTip = viewModel.getShouldShowTip().getValue();

        if (shouldShowTip != null && preferencesCreated)
            if (shouldShowTip)
                getPreferenceScreen().addPreference(tipView);
            else
                getPreferenceScreen().removePreference(tipView);
    }

}
