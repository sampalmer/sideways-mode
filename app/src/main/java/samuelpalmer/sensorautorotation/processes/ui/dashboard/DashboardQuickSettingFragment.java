package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.QuickSettingService;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.testing.FieldConstants;

/**
 * The contents of the main dashboard when we want to suggest that the user adds the quick settings
 * tile.
 */
@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class DashboardQuickSettingFragment extends Fragment {

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_dashboard_quick_setting, container, false);
        
        TextView description = layout.findViewById(R.id.dashboard_quick_setting_description);
        description.setText(getString(R.string.explanation_quick_setting, getText(R.string.app_name_full)));
        
        Button addButton = layout.findViewById(R.id.dashboard_quick_setting_add);
        addButton.setOnClickListener(view -> addTile());

        return layout;
    }

    @RequiresApi(api = 33)
    private void addTile() {
        QuickSettingService.promptUserToAddTile(requireContext(), tileAddResult -> {
            if (tileAddResult == StatusBarManager.TILE_ADD_REQUEST_ERROR_REQUEST_IN_PROGRESS || tileAddResult == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED || tileAddResult == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED || tileAddResult == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED) {
                // The goal here was to suggest the tile to the user, so we don't mind whether or not the user actually added it
                SharedPreferences prefs = ApplicationSettings.getSharedPreferences(requireContext());
                prefs
                        .edit()
                        .putBoolean(ApplicationSettings.KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN, true)
                        .apply();
            } else {
                // Something went wrong. This is probably a bug in our code or the OS.
                FieldConstants<StatusBarManager> errorConstants = new FieldConstants<>(StatusBarManager.class, "TILE_ADD_REQUEST_");
                String errorName = errorConstants.getName(tileAddResult);
                throw new RuntimeException("Error prompting user to add quick setting tile: " + errorName);
            }
        });
    }

}
