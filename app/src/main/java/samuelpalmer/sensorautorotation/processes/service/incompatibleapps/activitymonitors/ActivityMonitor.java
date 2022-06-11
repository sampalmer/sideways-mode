package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.utilities.ObjectExtensions;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Keeps track of which activity the user currently has open
 */
public abstract class ActivityMonitor extends EventRegistrarAdaptor {
    
    private static final String TAG = ActivityMonitor.class.getSimpleName();

    final Context context;

    @Nullable
    ComponentName lastActivity;

    ActivityMonitor(Context context) {
        if (context == null)
            throw new IllegalArgumentException();

        this.context = context;
    }

    /**
     * @return The top-most activity or <code>null</code> if unknown.
     */
    @Nullable
    public ComponentName currentOrNull() {
        if (!hasSubscriptions())
            throw new IllegalStateException("Can't detect the activity when not started");

        return lastActivity;
    }

    public abstract boolean hasPermission();

    /**
     * @implNote Make sure to always return the same instance here so subscription & unsubscription works properly.
     */
    @NonNull
    public abstract EventRegistrarAdaptor onPermissionChange();
    public abstract Intent makeAcquirePermissionIntent();
    public abstract String makeAcquirePermissionQuestion();
    public abstract String makeAcquirePermissionExplanation();

    void setTopActivity(@Nullable ComponentName activityOrNull, boolean sendUpdate) {
        if (!ObjectExtensions.areEqual(activityOrNull, lastActivity)) {
            // Not logging specific activity in Production builds since that would be a privacy violation since logs are included in error reports.
            String logMessage = "Activity changed";
            if (BuildConfig.DEBUG)
                logMessage += " to " + (activityOrNull == null ? null : activityOrNull.flattenToShortString());
            Log.i(TAG, logMessage);

            lastActivity = activityOrNull;

            if (sendUpdate)
                report();
        }
    }

    /**
     * Make sure to call {@link #setTopActivity(ComponentName, boolean)} with the current activity if possible.
     */
    @Override
    protected void subscribeToAdaptee() {
        Log.i(TAG, "Using " + getClass().getSimpleName()); // This could be useful for troubleshooting
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        lastActivity = null; // We no longer know if this is the current activity since we're now unsubscribed
    }

    public static ActivityMonitor create(Context context) {
        if (RunningTasksActivityMonitor.isSupported())
            // getRunningTasks() is pretty-much the closest thing we can get to a purpose-built API for this, so it's the first preference
            return new RunningTasksActivityMonitor(context);
        else if (UsageStatsActivityMonitor.isSupported(context))
            // This is out next-best bet since it uses polling, which is more reliable than callbacks
            return new UsageStatsActivityMonitor(context);
        else
            // This is our last resort since it relies on callbacks which can sometimes not occur or occur out-of-order
            return new AccessibilityServiceActivityMonitor(context);
    }

    @Nullable
    public String makeAcquirePermissionCancelledMessage() { return null; }
}
