package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.NullEventRegistrarAdaptor;

/**
 * {@link ActivityMonitor} that detects the current activity using {@link ActivityManager#getRunningTasks(int)}
 */
class RunningTasksActivityMonitor extends PollingActivityMonitor {

    static boolean isSupported() {
        return Build.VERSION.SDK_INT < 21;
    }

    private final ActivityManager activityManager;
    private final NullEventRegistrarAdaptor onPermissionChange;

    RunningTasksActivityMonitor(Context context) {
        super(context);
        activityManager = ContextCompat.getSystemService(context, ActivityManager.class);
        onPermissionChange = new NullEventRegistrarAdaptor();
    }

    @SuppressLint("NewApi") // The SDK thinks the topActivity field is an API 29 field, when in fact it has been available since API 14 and maybe earlier
    @Override
    protected ComponentName poll() {
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
        if (runningTasks != null && !runningTasks.isEmpty()) {
            ActivityManager.RunningTaskInfo currentTask = runningTasks.get(0);
            if (currentTask != null)
                return currentTask.topActivity;
        }

        Log.w(RunningTasksActivityMonitor.class.getSimpleName(), "Couldn't determine the current activity");
        return null;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @NonNull
    @Override
    public EventRegistrarAdaptor onPermissionChange() {
        return onPermissionChange;
    }

    @Override
    public Intent makeAcquirePermissionIntent() {
        // We always have permission since it's declared in the manifest and runtime permissions don't exist in these versions of Android
        return null;
    }

    @Override
    public String makeAcquirePermissionQuestion() {
        // We always have permission since it's declared in the manifest and runtime permissions don't exist in these versions of Android
        return null;
    }

    @Override
    public String makeAcquirePermissionExplanation() {
        // We always have permission since it's declared in the manifest and runtime permissions don't exist in these versions of Android
        return null;
    }

}
