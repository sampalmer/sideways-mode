package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.Manifest;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.custompreferences.WhenUprightPreference;

/**
 * The main settings screen contents
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {

    private WhenUprightPreference notificationPreference;

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

        // Android 13 has the app notifications blocked by default, so we need to prompt the user to unblock them
        // if they want the controls notification
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPreference = findPreference(ApplicationSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT);
            assert notificationPreference != null;
            notificationPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean alwaysShowControlsNotification = !WhenUprightPreference.stringToBoolean((String) newValue);
                if (alwaysShowControlsNotification) {
                    NotificationManager notificationManager = ContextCompat.getSystemService(requireContext(), NotificationManager.class);
                    assert notificationManager != null;
                    if (!notificationManager.areNotificationsEnabled()) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                        return false;
                    }
                }

                return true;
            });
        }
    }

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), wasGranted -> {
        if (!wasGranted)
            return;

        String keepNotification = WhenUprightPreference.booleanToString(false);
        notificationPreference.setValue(keepNotification);
    });

}
