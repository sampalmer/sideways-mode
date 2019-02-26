package samuelpalmer.sensorautorotation.processes.service.uninstall;

import android.content.Context;
import android.content.SharedPreferences;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Controls when {@link UninstallerMonitor} runs since it might introduce a bit of lag.
 */
public class UninstallerManager extends State {

    private final Context context;
    private final ActivityMonitor activityMonitor;
    private final SharedPreferences serviceSettings;

    private UninstallerMonitor monitor;

    public UninstallerManager(Context context, ActivityMonitor activityMonitor) {
        this.context = context;
        this.activityMonitor = activityMonitor;

        serviceSettings = ServiceSettings.getSharedPreferences(context);
    }

    @Override
    protected void start() {
        serviceSettings.registerOnSharedPreferenceChangeListener(prefChanged);
        sync();
    }

    @Override
    protected void stop() {
        serviceSettings.unregisterOnSharedPreferenceChangeListener(prefChanged);
        ensureMonitorStopped();
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener prefChanged = (sharedPreferences, key) -> {
        if (!isStarted())
            return;

        if (key.equals(ServiceSettings.KEY_UNINSTALLING_TIP_SHOWN))
            sync();
    };

    private void sync() {
        boolean tipShown = serviceSettings.getBoolean(ServiceSettings.KEY_UNINSTALLING_TIP_SHOWN, false);

        if (tipShown)
            ensureMonitorStopped();
        else
            ensureMonitorStarted();
    }

    private void ensureMonitorStarted() {
        if (monitor == null) {
            monitor = new UninstallerMonitor(context, activityMonitor);
            monitor.ensureStarted();
        }
    }

    private void ensureMonitorStopped() {
        if (monitor != null) {
            monitor.ensureStopped();
            monitor = null;
        }
    }

}
