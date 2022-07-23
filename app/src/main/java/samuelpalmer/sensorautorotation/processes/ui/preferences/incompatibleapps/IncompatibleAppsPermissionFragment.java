package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.PermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Provides UI and logic to help user grant permissions for the incompatible apps feature.
 */
@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class IncompatibleAppsPermissionFragment extends Fragment {

    private ActivityMonitor activityMonitor;
    private PermissionMonitor enforcementPermissionMonitor;

    private boolean isGrantingPermission;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMonitor = ActivityMonitor.create(requireContext());
        enforcementPermissionMonitor = ScreenOrientationEnforcer.makePermissionMonitor(requireContext());

        activityMonitor.onPermissionChange().subscribe(activityMonitorPermissionChanged);
        enforcementPermissionMonitor.subscribe(enforcementPermissionChanged);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        enforcementPermissionMonitor.unsubscribe(enforcementPermissionChanged);
        activityMonitor.onPermissionChange().unsubscribe(activityMonitorPermissionChanged);
    }

    public void grantPermissions() {
        if (!activityMonitor.hasPermission()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireActivity())
                    .setTitle(activityMonitor.makeAcquirePermissionTitle())
                    .setMessage(activityMonitor.makeAcquirePermissionExplanation())
                    .setOnCancelListener(dialog -> {})
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {});

            Intent acquirePermissionIntent = activityMonitor.makeAcquirePermissionIntent();
            if (acquirePermissionIntent != null) {
                dialogBuilder
                        .setPositiveButton(R.string.permission_needed_allow, (dialog, which) -> {
                            activityPermissionLauncher.launch(acquirePermissionIntent);
                            isGrantingPermission = true;
                        });
            }

            dialogBuilder.show();
        } else
            grantOverlayPermission();
    }

    private final ActivityResultLauncher<Intent> activityPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (activityMonitor.hasPermission())
                grantOverlayPermission();
        }
    });

    @SuppressLint("NewApi") // Because we're only sending the user to the permission screen on supported devices
    private void grantOverlayPermission() {
        if (!enforcementPermissionMonitor.hasPermission()) {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.permission_needed_overlays_summary)
                    .setMessage(getString(R.string.permission_needed_overlays_description, getText(R.string.app_name_full)))
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                    .setPositiveButton(R.string.permission_needed_allow, (dialog, which) -> {
                        Intent intent = SystemIntents.drawOnTopOfApps(getActivity());
                        startActivity(intent);
                        isGrantingPermission = true;
                    })
                    .show();
        }
    }

    private final EventHandler activityMonitorPermissionChanged = () -> {
        if (activityMonitor.hasPermission())
            onPermissionGranted();
    };

    private final EventHandler enforcementPermissionChanged = () -> {
        if (enforcementPermissionMonitor.hasPermission())
            onPermissionGranted();
    };

    private void onPermissionGranted() {
        if (isGrantingPermission) {
            FragmentActivity activity = getActivity();
            if (activity != null)
                ActivityUtils.finishActivitiesOnTopOf(activity);

            isGrantingPermission = false;
        }
    }

}
