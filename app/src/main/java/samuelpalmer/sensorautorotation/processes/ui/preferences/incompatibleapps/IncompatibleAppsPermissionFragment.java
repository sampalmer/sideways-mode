package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

/**
 * Provides UI and logic to help user grant permissions for the incompatible apps feature.
 */
@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class IncompatibleAppsPermissionFragment extends Fragment {

    private static final int REQUEST_CODE_ACTIVITY_MONITOR = 1;

    private ActivityMonitor activityMonitor;
    private EventRegistrar enforcementPermissionMonitor;

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
        if (!activityMonitor.hasPermission())
            new AlertDialog.Builder(requireActivity())
                    .setTitle(activityMonitor.makeAcquirePermissionQuestion())
                    .setMessage(activityMonitor.makeAcquirePermissionExplanation())
                    .setOnCancelListener(dialog -> {})
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {})
                    .setPositiveButton(R.string.permission_needed_allow, (dialog, which) -> {
                        Intent intent = activityMonitor.makeAcquirePermissionIntent();
                        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_MONITOR);
                        isGrantingPermission = true;
                    })
                    .show();
        else
            grantOverlayPermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACTIVITY_MONITOR) {
            if (activityMonitor.hasPermission())
                grantOverlayPermission();
            else {
                String cancelledMessage = activityMonitor.makeAcquirePermissionCancelledMessage();

                if (cancelledMessage != null)
                    Toast
                        .makeText(requireActivity(), cancelledMessage, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void grantOverlayPermission() {
        if (!ScreenOrientationEnforcer.hasPermissionToStart(requireContext())) {
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
        if (ScreenOrientationEnforcer.hasPermissionToStart(requireContext()))
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
