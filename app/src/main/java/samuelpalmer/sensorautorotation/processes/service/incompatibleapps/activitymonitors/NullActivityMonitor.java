package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.NullEventRegistrarAdaptor;

/**
 * Placeholder activity monitor for use when no working activity monitor is available.
 * This activity monitor never has permission and never detects activity changes.
 */
class NullActivityMonitor extends ActivityMonitor {

    private final NullEventRegistrarAdaptor onPermissionChange;

    NullActivityMonitor(Context context) {
        super(context);
        onPermissionChange = new NullEventRegistrarAdaptor();
    }

    @Override
    public boolean hasPermission() {
        return false;
    }

    @NonNull
    @Override
    public EventRegistrarAdaptor onPermissionChange() {
        return onPermissionChange;
    }

    @Override
    public Intent makeAcquirePermissionIntent() {
        return null;
    }

    @Override
    public String makeAcquirePermissionTitle() {
        return context.getString(R.string.device_problem);
    }

    @Override
    public String makeAcquirePermissionExplanation() {
        CharSequence appNameFull = context.getText(R.string.app_name_full);
        return context.getString(R.string.permission_needed_no_activity_monitor_description, appNameFull);
    }

}
