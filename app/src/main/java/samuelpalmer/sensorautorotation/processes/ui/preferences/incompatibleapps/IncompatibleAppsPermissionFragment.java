package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceScreen;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.ActivityUtils;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

@SuppressWarnings("WeakerAccess") // Because fragments must be public
public class IncompatibleAppsPermissionFragment extends Fragment {

    private static final int REQUEST_CODE_ACTIVITY_MONITOR = 1;

    private ActivityMonitor activityMonitor;
    private EventRegistrar enforcementPermissionMonitor;

    private PreferenceScreen preferenceScreen;
    private Context context; //Must use this context instead of getActivity() since the latter isn't always available soon enough
    private PermissionTipViewPreference tipView;

    private boolean isGrantingPermission;

    public void initialise(PreferenceScreen preferenceScreen) {
        this.preferenceScreen = preferenceScreen;
        this.context = preferenceScreen.getContext();

        activityMonitor = ActivityMonitor.create(context);
        enforcementPermissionMonitor = ScreenOrientationEnforcer.makePermissionMonitor(context);
        
        tipView = (PermissionTipViewPreference) preferenceScreen.findPreference("incompatibleAppsPermission");
        tipView.setOnPreferenceClickListener(preference -> {
            grantPermissions();
            return true;
        });

        activityMonitor.onPermissionChange().subscribe(activityMonitorPermissionChanged);
        enforcementPermissionMonitor.subscribe(enforcementPermissionChanged);

        updateTip(); //Updating the visibility of the tip in onStart() isn't early enough when switching between split and fullscreen on Android 7.1
    }

    @Override
    public void onStart() {
        super.onStart();

        if (preferenceScreen == null)
            throw new IllegalStateException("Can't initialise because initialise() hasn't been called");

        updateTip();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        enforcementPermissionMonitor.unsubscribe(enforcementPermissionChanged);
        activityMonitor.onPermissionChange().unsubscribe(activityMonitorPermissionChanged);
    }

    private void grantPermissions() {
        if (!activityMonitor.hasPermission())
            new AlertDialog.Builder(context)
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
                        .makeText(context, cancelledMessage, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void grantOverlayPermission() {
        if (!ScreenOrientationEnforcer.hasPermissionToStart(context)) {
            new AlertDialog.Builder(context)
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
        updateTip();

        if (activityMonitor.hasPermission())
            onPermissionGranted();
    };

    private final EventHandler enforcementPermissionChanged = () -> {
        updateTip();

        if (ScreenOrientationEnforcer.hasPermissionToStart(context))
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

    private void updateTip() {
        if (activityMonitor.hasPermission() && ScreenOrientationEnforcer.hasPermissionToStart(context))
            preferenceScreen.removePreference(tipView);
        else
            preferenceScreen.addPreference(tipView);
    }

}
