package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;
import samuelpalmer.sensorautorotation.R;

public class MainSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String prefsName = ApplicationSettings.getSharedPreferencesName(requireContext());
        getPreferenceManager().setSharedPreferencesName(prefsName);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_main);

        Preference incompatibleAppsPreference = findPreference("incompatibleApps");
        assert incompatibleAppsPreference != null;
        incompatibleAppsPreference.setOnPreferenceClickListener(preference -> {
            RecyclerView prefsList = MainSettingsFragment.super.getListView();
            NavController navController = Navigation.findNavController(prefsList);
            navController.navigate(R.id.action_mainSettingsFragment_to_incompatibleAppsFragment);
            return true;
        });
    }

}
