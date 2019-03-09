package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

import java.util.Collections;
import java.util.Set;

/**
 * Represents the "incompatible apps" feature. Dynamically overrides the screen orientation
 * depending on the currently open activity.
 */
public class IncompatibleAppsMonitor extends State {

    private final ActivityMonitor activityMonitor;
    private final PermissionNotificationManager permissionNotificationManager;
    private final ScreenOrientationEnforcer orientationEnforcer;
    private final LockScreenMonitor lockScreenDetector;
    private final SharedPreferences prefs;
    private final Context context;
    private Set<String> incompatibleAppPackages;
    
    public IncompatibleAppsMonitor(Context context, ActivityMonitor activityMonitor, PermissionNotificationManager permissionNotificationManager) {
        this.context = context;
        this.activityMonitor = activityMonitor;
        this.permissionNotificationManager = permissionNotificationManager;
        orientationEnforcer = new ScreenOrientationEnforcer(this.context);
        this.lockScreenDetector = new LockScreenMonitor(this.context);
        prefs = ServiceSettings.getSharedPreferences(this.context);
    }

    @Override
    protected void start() {
        lockScreenDetector.subscribe(enforcementUpdater);
        activityMonitor.subscribe(enforcementUpdater);
        prefs.registerOnSharedPreferenceChangeListener(incompatibleAppsChanged);

        updatePackages();
        tryToStartEnforcer();
    }

    private void tryToStartEnforcer() {
        if (ScreenOrientationEnforcer.hasPermissionToStart(context)) {
            orientationEnforcer.ensureStarted();
            updateEnforcement();
        }
    }

    @Override
    protected void stop() {
        prefs.unregisterOnSharedPreferenceChangeListener(incompatibleAppsChanged);
        activityMonitor.unsubscribe(enforcementUpdater);
        lockScreenDetector.unsubscribe(enforcementUpdater);
        orientationEnforcer.ensureStopped();
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener incompatibleAppsChanged = (sharedPreferences, s) -> {
        if (isStarted() && s.equals(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES)) {
            updatePackages();
            updateEnforcement();
        }
    };

    private void updatePackages() {
        incompatibleAppPackages = prefs.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
    }

    private void updateEnforcement() {
        if (orientationEnforcer.isStarted()) {
            //Note that the permission may have been revoked at this point, but it doesn't cause a crash. Not checking it here since it could introduce lag.
            orientationEnforcer.setEnforced(shouldEnforce());
        }
        else {
            tryToStartEnforcer();
            if (orientationEnforcer.isStarted())
                permissionNotificationManager.check(); //Android doesn't tell us when the permission is granted, so we'll just keep checking it so the error notification can be cancelled
        }
    }

    private boolean shouldEnforce() {
        ComponentName currentActivity = activityMonitor.currentOrNull();

        return
            currentActivity != null
            &&
            incompatibleAppPackages.contains(currentActivity.getPackageName())
            &&
            //Not allowing orientation enforcement on the lock screen in case it breaks it and makes it hard to unlock the device
            !lockScreenDetector.isOnLockScreen();
    }

    private final EventHandler enforcementUpdater = this::updateEnforcement;

}
