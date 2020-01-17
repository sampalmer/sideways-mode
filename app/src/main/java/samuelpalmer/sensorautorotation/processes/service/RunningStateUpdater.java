package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class RunningStateUpdater extends State {
    
    private final OrientationCalculator calculator;
    private final RotationService service;
    private final SharedPreferences prefs;
    private final ServiceNotification notificationBuilder;

    public RunningStateUpdater(Context context, OrientationCalculator calculator, RotationService service) {
        this.calculator = calculator;
        this.service = service;
        prefs = ServiceSettings.getSharedPreferences(context);
        notificationBuilder = new ServiceNotification(context);
    }

    @Override
    protected void start() {
        updateDisplays.update();
        calculator.onAnythingChanged.subscribe(updateDisplays);
        prefs.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    protected void stop() {
        prefs.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        calculator.onAnythingChanged.unsubscribe(updateDisplays);
    }

    /**
     * Updates the notification when the value of {@link ServiceSettings#KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT} changes.
     * This is needed because this setting influences the notification icons.
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (ServiceSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT.equals(key))
                updateNotification(lastRunningState);
        }
    };

    private final EventHandler updateDisplays = new EventHandler() {
        @Override
        public void update() {
            ApplicationStatus.RunningState runningState = calculator.runningState();

            if (!runningState.equals(lastRunningState)) {
                updateNotification(runningState);
                lastRunningState = runningState;
                onStateChanged.report();
            }
        }
    };

    private void updateNotification(ApplicationStatus.RunningState runningState) {
        Notification notification = notificationBuilder.build(runningState);

        // Running the service in the foreground reduces the chance of the process being killed.
        // This is important, since the process being killed would result in the screen no longer rotating.
        if (Build.VERSION.SDK_INT >= 29)
            service.startForeground(NotificationIds.SERVICE, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        else
            service.startForeground(NotificationIds.SERVICE, notification);
    }

    @Nullable
    public ApplicationStatus.RunningState getLastStatusRunningState() {
        return lastRunningState;
    }
    
    public final UpdatingEventRegistrar onStateChanged = new UpdatingEventRegistrar();
    
    private ApplicationStatus.RunningState lastRunningState;
}
