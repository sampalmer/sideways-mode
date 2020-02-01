package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDeepLinkBuilder;

import java.util.Collections;
import java.util.Set;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SettingsActivity;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

/**
 * Issues a notification if the permissions needed for the "Incompatible apps" feature are
 * missing but required.
 *
 * An example where this could be useful is:
 * Samsung's app optimization feature might disable the accessibility service, although I don't
 * know whether that is possible since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272).
 * Hopefully this notification will be sufficient for handling that situation.
 */
public class PermissionNotificationManager extends State {

    private final Context context;
    private final ActivityMonitor activityMonitor;
    private final EventRegistrar enforcementPermissionMonitor;
    private final NotificationManager notificationManager;
    private final SharedPreferences prefs;

    private boolean isRunning = false;

    public PermissionNotificationManager(Context context, ActivityMonitor activityMonitor) {
        this.context = context;
        this.activityMonitor = activityMonitor;
        enforcementPermissionMonitor = ScreenOrientationEnforcer.makePermissionMonitor(context);

        notificationManager = ContextCompat.getSystemService(this.context, NotificationManager.class);
        prefs = ServiceSettings.getSharedPreferences(this.context);
    }

    @Override
    protected void start() {
        prefs.registerOnSharedPreferenceChangeListener(incompatibleAppsChanged);
        activityMonitor.onPermissionChange().subscribe(permissionChanged);
        enforcementPermissionMonitor.subscribe(permissionChanged);

        isRunning = true;
        check();
}
    
    @Override
    protected void stop() {
        isRunning = false;
        check();

        enforcementPermissionMonitor.unsubscribe(permissionChanged);
        activityMonitor.onPermissionChange().unsubscribe(permissionChanged);
        prefs.unregisterOnSharedPreferenceChangeListener(incompatibleAppsChanged);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener incompatibleAppsChanged = (sharedPreferences, s) -> {
        if (isRunning && s.equals(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES))
            check();
    };
    
    private final EventHandler permissionChanged = this::check;
    
    public void check() {
        if (needsPermission()) {
            PendingIntent incompatibleAppsIntent = new NavDeepLinkBuilder(context)
                    .setComponentName(SettingsActivity.class)
                    .setGraph(R.navigation.settings)
                    .setDestination(R.id.incompatibleAppsFragment)
                    .createPendingIntent();

            Notification notification = NotificationBuilderFactory
                    .create(context)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .setContentIntent(incompatibleAppsIntent)
                    .setContentTitle(context.getText(R.string.permission_needed_summary))
                    .setContentText(context.getText(R.string.permission_touch_to_fix))
                    .setSmallIcon(R.drawable.notification_error)
                    .setChannelId(NotificationChannels.ID_PERMISSIONS)
                    .build();

            notificationManager.notify(NotificationIds.PERMISSION, notification);
        }
        else
            notificationManager.cancel(NotificationIds.PERMISSION);
    }
    
    private boolean needsPermission() {
        if (isRunning) {
            Set<String> incompatibleAppPackages = prefs.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());

            //noinspection RedundantIfStatement
            if (!incompatibleAppPackages.isEmpty() && !(activityMonitor.hasPermission() && ScreenOrientationEnforcer.hasPermissionToStart(context)))
                return true;
        }
        
        return false;
    }
    
}
