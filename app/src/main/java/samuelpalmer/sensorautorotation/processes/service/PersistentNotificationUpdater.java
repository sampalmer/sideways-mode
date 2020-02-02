package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.DarkThemeMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Implementation of {@link ApplicationSettings#KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT}. Updates
 * the persistent notification when something changes that cannot be detected by an implicit
 * broadcast. For implicit broadcast updates, see {@link PersistentNotificationSyncReceiver}.
 */
public final class PersistentNotificationUpdater extends State {

    private final Context context;
    private final SharedPreferences serviceSettings;
    private final DarkThemeMonitor darkThemeMonitor;

    public PersistentNotificationUpdater(Context context) {
        this.context = context;
        serviceSettings = ServiceSettings.getSharedPreferences(context);
        darkThemeMonitor = new DarkThemeMonitor(context);
    }

    @Override
    protected void start() {
        serviceSettings.registerOnSharedPreferenceChangeListener(serviceSettingsChanged);
        darkThemeMonitor.subscribe(darkThemeChanged);
    }

    @Override
    protected void stop() {
        darkThemeMonitor.unsubscribe(darkThemeChanged);
        serviceSettings.unregisterOnSharedPreferenceChangeListener(serviceSettingsChanged);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener serviceSettingsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (ServiceSettings.KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT.equals(key)) {
                // The persistent notification setting changed, so we might need to update the service notification
                sync(context);
            }
        }
    };

    private final EventHandler darkThemeChanged = new EventHandler() {
        @Override
        public void update() {
            // The notification needs an update since the theme changed.
            // There's no guarantee we'll actually get here since this process might not be running
            // at the time of the theme changed, but there's not much more we can do.
            sync(context);
        }
    };

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
