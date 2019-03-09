package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.Context;
import android.content.SharedPreferences;
import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;

import java.util.Set;

/**
 * Turns the incompatible apps feature on and off as needed.
 *
 * The enforcement feature isn't kept on all the time since:
 * - This would result in unnecessary work
 * - If the feature causes a problem, such as a crash, the user might be able to observe that the problem only started when they enabled this feature.
 */
public class IncompatibleAppsManager extends State {

    private final IncompatibleAppsMonitor monitor;
    private final SharedPreferences prefs;
    private final PermissionNotificationManager permissionNotificationManager;

    public IncompatibleAppsManager(Context context, ActivityMonitor activityMonitor, LockScreenMonitor lockScreenMonitor) {
        permissionNotificationManager = new PermissionNotificationManager(context, activityMonitor);
        monitor = new IncompatibleAppsMonitor(context, activityMonitor, permissionNotificationManager, lockScreenMonitor);
        prefs = ServiceSettings.getSharedPreferences(context);
    }

    @Override
    protected void start() {
        permissionNotificationManager.ensureStarted();
        prefs.registerOnSharedPreferenceChangeListener(incompatibleAppsChanged);

        updateMonitorState();
    }

    @Override
    protected void stop() {
        prefs.unregisterOnSharedPreferenceChangeListener(incompatibleAppsChanged);
        monitor.ensureStopped();
        permissionNotificationManager.ensureStopped();
    }

    // This must be stored in a field to prevent garbage collection.
    private final SharedPreferences.OnSharedPreferenceChangeListener incompatibleAppsChanged = (sharedPreferences, s) -> {
        if (isStarted() && s.equals(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
            updateMonitorState();
    };

    private void updateMonitorState() {
        Set<String> incompatibleAppPackages = prefs.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, null);

        if (incompatibleAppPackages != null && !incompatibleAppPackages.isEmpty())
            monitor.ensureStarted();
        else
            monitor.ensureStopped();
    }

}
