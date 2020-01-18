package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class RunningStateUpdater extends State {
    
    private final OrientationCalculator calculator;
    private final RotationService service;
    private final ServiceNotification notificationBuilder;
    private final SharedPreferences prefs;
    private final NotificationManager notificationManager;

    public RunningStateUpdater(Context context, OrientationCalculator calculator, RotationService service) {
        this.calculator = calculator;
        this.service = service;
        notificationBuilder = new ServiceNotification(context);
        prefs = ServiceSettings.getSharedPreferences(context);
        notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
    }

    @Override
    protected void start() {
        updateDisplays.update();
        calculator.onAnythingChanged.subscribe(updateDisplays);
    }

    @Override
    protected void stop() {
        calculator.onAnythingChanged.unsubscribe(updateDisplays);

        // Detach notification from service
        boolean closeNotificationWhenUpright = prefs.getBoolean(ServiceSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT, true);

        // TODO: If prior to Android N, then post a dummy notification to keep the notification drawer open

        // Prior to Android N, the OS will *always* remove the notification when the service is stopped, even if we tried to detach it from the service.
        // So in those OS versions, our best option is to completely remove the notification and re-post it straight away.
        boolean removeNotification = closeNotificationWhenUpright || Build.VERSION.SDK_INT <= 23;

        ServiceCompat.stopForeground(service, removeNotification ? ServiceCompat.STOP_FOREGROUND_REMOVE : ServiceCompat.STOP_FOREGROUND_DETACH);

        if (!closeNotificationWhenUpright){
            // Update notification to "upright" / "off" state
            Notification notification = notificationBuilder.build(null);
            notificationManager.notify(NotificationIds.SERVICE, notification);
        }

        // TODO: If prior to Android N, then remove the dummy notification
    }

    private final EventHandler updateDisplays = new EventHandler() {
        @Override
        public void update() {
            ApplicationStatus.RunningState runningState = calculator.runningState();

            if (!runningState.equals(lastRunningState)) {
                Notification notification = notificationBuilder.build(runningState);

                // Running the service in the foreground reduces the chance of the process being killed.
                // This is important, since the process being killed would result in the screen no longer rotating.
                if (Build.VERSION.SDK_INT >= 29)
                    service.startForeground(NotificationIds.SERVICE, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
                else
                    service.startForeground(NotificationIds.SERVICE, notification);

                lastRunningState = runningState;
                onStateChanged.report();
            }
        }
    };
    
    @Nullable
    public ApplicationStatus.RunningState getLastStatusRunningState() {
        return lastRunningState;
    }
    
    public final UpdatingEventRegistrar onStateChanged = new UpdatingEventRegistrar();
    
    private ApplicationStatus.RunningState lastRunningState;
}
