package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.AppOpMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * {@link ActivityMonitor} that detects the current activity using {@link UsageStatsManager}
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class UsageStatsActivityMonitor extends PollingActivityMonitor {

    private static final Intent acquirePermissionIntent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
    private static final int ACTIVITY_IN_FOREGROUND;

    static {
        if (Build.VERSION.SDK_INT >= 29)
            ACTIVITY_IN_FOREGROUND = UsageEvents.Event.ACTIVITY_RESUMED;
        else {
            ACTIVITY_IN_FOREGROUND = UsageEvents.Event.MOVE_TO_FOREGROUND;
        }
    }

    static boolean isSupported(Context context) {
        if (Build.VERSION.SDK_INT < 21)
            return false;

        PackageManager packageManager = context.getPackageManager();

        // Some devices don't have the usage access settings screen or don't have an intent to go there.
        // For example, Samsung Galaxy S5 SM-G900I running G900IZTU1BOA1 firmware has the screen but the intent doesn't work.
        // So these users won't have any easy way to grant the permission and hence can't use this feature.
        ResolveInfo resolveInfo;
        int resolveFlags = 0;
        if (Build.VERSION.SDK_INT >= 33) {
            resolveInfo = packageManager.resolveActivity(acquirePermissionIntent, PackageManager.ResolveInfoFlags.of(resolveFlags));
        } else {
            //noinspection deprecation
            resolveInfo = packageManager.resolveActivity(acquirePermissionIntent, resolveFlags);
        }

        return resolveInfo != null;
    }

    private final UsageStatsManager usageStatsManager;
    private final AppOpMonitor permissionMonitor;

    private long lastActivityTime;

    @SuppressLint("InlinedApi") // The USAGE_STATS_SERVICE constant was introduced in API 22 but still works fine on API 21.
    UsageStatsActivityMonitor(Context context) {
        super(context);

        // Not using ContextCompat.getSystemService since it returns null on API 21
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        permissionMonitor = new AppOpMonitor(context, AppOpsManager.OPSTR_GET_USAGE_STATS);
    }

    @Override
    protected ComponentName poll() {
        long nowMillis = System.currentTimeMillis();

        // Only looking back as far as the last known activity to minimise resource usage
        UsageEvents usageEvents = usageStatsManager.queryEvents(lastActivityTime > 0 ? lastActivityTime - 1 : 0, nowMillis);
        boolean gotEvents = false;
        long latestEventTime = -1;
        String latestEventPackage = null;
        String latestEventClassName = null;

        if (usageEvents != null) {
            UsageEvents.Event event = new UsageEvents.Event();

            while (usageEvents.hasNextEvent()) {
                gotEvents = true;

                // Apparently you need to always loop through all available events to avoid a crash: https://stackoverflow.com/a/50896301/238753
                usageEvents.getNextEvent(event);

                if (event.getEventType() == ACTIVITY_IN_FOREGROUND && event.getTimeStamp() > latestEventTime) {
                    // It looks safe to assume that MOVE_TO_FOREGROUND is for an activity: https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/master/services/core/java/com/android/server/am/ActivityManagerService.java
                    latestEventPackage = event.getPackageName();
                    latestEventClassName = event.getClassName();
                    latestEventTime = event.getTimeStamp();
                }
            }
        }

        if (latestEventTime > lastActivityTime)
            lastActivityTime = latestEventTime;

        if (latestEventPackage != null) {
            String className = latestEventClassName;
            if (className == null) {
                Log.w(getClass().getSimpleName(), "Top activity has no class name" + (BuildConfig.DEBUG ? ": " + latestEventPackage : ""));
                className = ""; // This is to prevent ComponentName from complaining
            }

            return new ComponentName(latestEventPackage, className);
        }
        else {
            if (!gotEvents) {
                // Once this happened on an Android emulator. Usage permission was granted, but the
                // OS didn't provide any usage events at all. Restarting the device fixed the issue.

                if (lastActivity != null) {
                    // Only logging this if we previously knew the activity.
                    // This is to prevent the log message from repeating every time this is polled.

                    // Apparently some devices report that permission is granted when it's actually not, resulting in this situation.
                    // See https://stackoverflow.com/a/43583005/238753.
                    Log.w(getClass().getSimpleName(), "Didn't get any usage events. Usage permission is probably missing.");
                }

                return null; // We're in the dark about which app is on top since polling isn't working at the moment
            }
            else {
                // We got some data but couldn't find the last-opened activity. This shouldn't happen.
                // It's probably safe to assume that the last known activity is still the current one.
                return lastActivity;
            }
        }
    }

    @Override
    public boolean hasPermission() {
        return permissionMonitor.hasPermission();
    }

    @NonNull
    @Override
    public EventRegistrarAdaptor onPermissionChange() {
        return permissionMonitor;
    }

    @Override
    public Intent makeAcquirePermissionIntent() {
        return acquirePermissionIntent;
    }

    @Override
    public String makeAcquirePermissionQuestion() {
        return context.getString(R.string.permission_needed_usage_summary);
    }

    @Override
    public String makeAcquirePermissionExplanation() {
        CharSequence appNameFull = context.getText(R.string.app_name_full);
        return context.getString(R.string.permission_needed_usage_description, appNameFull);
    }

}
