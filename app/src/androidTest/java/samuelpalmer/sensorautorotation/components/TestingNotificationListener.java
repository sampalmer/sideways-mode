package samuelpalmer.sensorautorotation.components;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.MainThread;
import androidx.core.os.HandlerCompat;

import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

public class TestingNotificationListener extends NotificationListenerService {

    /** Called on main thread */
    public static final UpdatingEventRegistrar onConnected = new UpdatingEventRegistrar();

    /** Called on main thread */
    public static final UpdatingEventRegistrar onDisconnected = new UpdatingEventRegistrar();

    /** Called on main thread */
    public static UpdatingEventRegistrar onNotificationsChanged = new UpdatingEventRegistrar();

    private static final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private static volatile TestingNotificationListener instance = null;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
        instance = this;
        mainHandler.post(onConnected::report);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
        instance = null;
        mainHandler.post(onDisconnected::report);
    }

    @MainThread
    public static boolean isConnected() {
        return instance != null;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
        mainHandler.post(onNotificationsChanged::report);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // WARNING: Not guaranteed to be called from main thread: https://developer.android.com/reference/android/service/notification/NotificationListenerService
        mainHandler.post(onNotificationsChanged::report);
    }

    @MainThread
    public static StatusBarNotification[] getNotifications() {
        if (instance == null)
            throw new IllegalStateException("Not connected");

        StatusBarNotification[] notifications = instance.getActiveNotifications();
        if (notifications == null)
            throw new RuntimeException("Couldn't get notifications");

        return notifications;
    }

}
