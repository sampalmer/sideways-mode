package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.utilities.DarkThemeMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class RunningStateUpdater extends State {

    private final Context context;
    private final OrientationCalculator calculator;
    private final RotationService service;
    private final ServiceNotificationUnblockedMonitor notificationUnblockedMonitor;
    private ServiceNotification notificationBuilder;
    private final SharedPreferences prefs;
    private final NotificationManager notificationManager;
    private final DarkThemeMonitor darkThemeMonitor;

    public RunningStateUpdater(Context context, OrientationCalculator calculator, RotationService service) {
        this.context = context;
        this.calculator = calculator;
        this.service = service;
        notificationBuilder = new ServiceNotification(context);
        prefs = ServiceSettings.getSharedPreferences(context);
        notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        darkThemeMonitor = new DarkThemeMonitor(context);
        notificationUnblockedMonitor = new ServiceNotificationUnblockedMonitor(context);
    }

    @Override
    protected void start() {
        anythingChanged.update();
        calculator.onAnythingChanged.subscribe(anythingChanged);
        darkThemeMonitor.subscribe(darkThemeChanged);
        notificationUnblockedMonitor.subscribe(notificationUnblocked);
    }

    @Override
    protected void stop() {
        notificationUnblockedMonitor.unsubscribe(notificationUnblocked);
        darkThemeMonitor.unsubscribe(darkThemeChanged);
        calculator.onAnythingChanged.unsubscribe(anythingChanged);

        // Detach notification from service
        boolean closeNotificationWhenUpright = prefs.getBoolean(ServiceSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT, true);

        // Prior to Android N, the OS will *always* remove the notification when the service is stopped, even if we tried to detach it from the service.
        // So in those OS versions, our best option is to completely remove the notification and re-post it straight away.
        boolean removeNotification = closeNotificationWhenUpright || Build.VERSION.SDK_INT <= 23;

        // If all notifications in the OS notification shade are removed, then the OS closes the notification shade.
        // The problem with this is we are only *temporarily* removing the notification, so from the user's perspective, it's not really being removed.
        // So we need to trick the OS into leaving the notification shade open.
        // We do this by briefly posting a dummy notification to ensure there is at least one notification in the shade while we re-post the service notification.
        boolean postDummyNotification = removeNotification && !closeNotificationWhenUpright;

        if (postDummyNotification) {
            Notification dummyNotification = buildDummyNotification();
            notificationManager.notify(NotificationIds.DUMMY, dummyNotification);
        }

        ServiceCompat.stopForeground(service, removeNotification ? ServiceCompat.STOP_FOREGROUND_REMOVE : ServiceCompat.STOP_FOREGROUND_DETACH);

        // Update notification to "upright" / "off" state
        // This also restores the notification if we had to remove it in stopForeground()
        // This is also needed to prevent Android from delaying 5 seconds from when the notification was last posted/updated before dismissing it on Google Pixel 3 XL Android 10 Build QQ1A.200205.002 and Android 11 Preview 1 emulator
        Notification notification = notificationBuilder.build(null);
        notificationManager.notify(NotificationIds.SERVICE, notification);

        if (closeNotificationWhenUpright)
            notificationManager.cancel(NotificationIds.SERVICE);

        if (postDummyNotification)
            notificationManager.cancel(NotificationIds.DUMMY);
    }

    private final EventHandler anythingChanged = new EventHandler() {
        @Override
        public void update() {
            ApplicationStatus.RunningState runningState = calculator.runningState();

            if (!runningState.equals(lastRunningState)) {
                updateDisplays(runningState);
                lastRunningState = runningState;
                onStateChanged.report();
            }
        }
    };

    private final EventHandler darkThemeChanged = new EventHandler() {
        @Override
        public void update() {
            // The notification builder caches the colour scheme, so we need to recreate it
            notificationBuilder = new ServiceNotification(context);

            // Since the notification uses a custom layout
            updateDisplays(lastRunningState);
        }
    };

    private final EventHandler notificationUnblocked = new EventHandler() {
        @Override
        public void update() {
            // The notification was just unblocked, so we'll re-post it
            updateDisplays(lastRunningState);
        }
    };

    private void updateDisplays(ApplicationStatus.RunningState runningState) {
        Notification notification = notificationBuilder.build(runningState);

        // Running the service in the foreground reduces the chance of the process being killed.
        // This is important, since the process being killed would result in the screen no longer rotating.
        if (Build.VERSION.SDK_INT >= 29)
            service.startForeground(NotificationIds.SERVICE, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        else
            service.startForeground(NotificationIds.SERVICE, notification);
    }

    private Notification buildDummyNotification() {
        return NotificationBuilderFactory.create(context)
                .setSmallIcon(R.drawable.app_empty) // So the user doesn't see the icon in the status bar
                .setCustomContentView(new RemoteViews(context.getPackageName(), R.layout.notification_dummy))
                .setPriority(NotificationCompat.PRIORITY_MIN) // To move the notification near the bottom of the list
                .setShowWhen(false) // Not really needed, but just-in-case
                .setWhen(1) // To move the notification below other notifications of equal priority
                .setVisibility(NotificationCompat.VISIBILITY_SECRET) // To hide the notification from the lock screen
                .setOngoing(true) // Prevents the "dismiss all" button from briefly appearing in the OS notification shade while this notification is visible
                .build();
    }

    @Nullable
    public ApplicationStatus.RunningState getLastStatusRunningState() {
        return lastRunningState;
    }
    
    public final UpdatingEventRegistrar onStateChanged = new UpdatingEventRegistrar();
    
    private ApplicationStatus.RunningState lastRunningState;
}
