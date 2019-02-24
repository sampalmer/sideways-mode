package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class UsageStatsActivityMonitor extends PollingActivityMonitor {

    static boolean isSupported(Context context) {
        if (Build.VERSION.SDK_INT < 21)
            return false;

        // Apparently some devices don't actually have the usage access settings screen.
        // So these users won't have any way to grant the permission and hence can't use this feature.
        // See https://stackoverflow.com/questions/27874224/android-content-activitynotfoundexception-no-activity-found-to-handle-intent
        // TODO: Test out this workaround: https://stackoverflow.com/questions/27330339/android-lollipop-know-if-app-as-usage-stats-access?noredirect=1&lq=1#comment54250337_29732133
        PackageManager packageManager = context.getPackageManager();
        // TODO: Consolidate the intent action with the one returned from makeAcquirePermissionIntent()
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        return packageManager.resolveActivity(intent, 0) != null;
    }

    private final UsageStatsManager usageStatsManager;
    private final UsageStatsPermissionMonitor permissionMonitor;

    private long lastActivityTime;

    @SuppressLint("InlinedApi") // The USAGE_STATS_SERVICE constant was introduced in API 22 but still works fine on API 21.
    UsageStatsActivityMonitor(Context context) {
        super(context);

        // Not using ContextCompat.getSystemService since it returns null on API 21
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        permissionMonitor = new UsageStatsPermissionMonitor(context);
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

                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND && event.getTimeStamp() > latestEventTime) {
                    // It looks safe to assume that MOVE_TO_FOREGROUND is for an activity: https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/master/services/core/java/com/android/server/am/ActivityManagerService.java
                    latestEventPackage = event.getPackageName();
                    latestEventClassName = event.getClassName();
                    latestEventTime = event.getTimeStamp();
                }
            }
        }

        if (!gotEvents) {
            // Apparently some devices report that permission is granted when it's actually not, resulting in this situation.
            // See https://stackoverflow.com/a/43583005/238753.
            Log.w(getClass().getSimpleName(), "Didn't get any usage events. Usage permission is probably missing.");
            // TODO: Bubble up a permission error since the user might have revoke the permission while the app was running
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
            // We couldn't find the last-opened activity. This shouldn't be possible.
            // It's probably safe to assume that the last known activity is still the current one.
            Log.w(getClass().getSimpleName(), "Didn't find the top activity. Re-using last known activity.");
            return lastActivity;
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
        return new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
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
