package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Implementation of {@link ApplicationSettings#KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT}
 */
public abstract class PersistentNotification {

    /**
     * If the app is turned off, then this ensures that the notification is present or missing as appropriate.
     * If the app is turned on, then this does nothing, since the notification is the responsibility of the service.
     */
    public static void sync(Context context) {
        SharedPreferences serviceSettings = ServiceSettings.getSharedPreferences(context);
        boolean isAppOn = serviceSettings.contains(ServiceSettings.KEY_PERSISTENT_STATE);

        if (!isAppOn) {
            // The app is currently off, so we need to manage the notification here.
            // (When the app is running, then it manages the notification internally, so no need to worry about the notification from out here.)
            NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
            assert notificationManager != null;

            boolean closeNotificationWhenUpright = serviceSettings.getBoolean(ServiceSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT, true);
            if (closeNotificationWhenUpright) {
                // The user has turned off the persistent notification, so we'll ensure the notification is gone
                notificationManager.cancel(NotificationIds.SERVICE);
            } else {
                // The user has turned on the persistent notification, so we'll ensure the notification is present
                Notification notification = new ServiceNotification(context).build(null);
                notificationManager.notify(NotificationIds.SERVICE, notification);
            }
        }
    }

}
