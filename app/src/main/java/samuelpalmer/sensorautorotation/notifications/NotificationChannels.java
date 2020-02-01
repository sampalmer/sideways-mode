package samuelpalmer.sensorautorotation.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.R;

public abstract class NotificationChannels {

    public static final String ID_CONTROLS = "controls";
    public static final String ID_UPGRADES = "upgrades";
    public static final String ID_ERRORS = "errors";
    public static final String ID_PERMISSIONS = "permissions";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setup(Context context) {
        Log.i(NotificationChannels.class.getSimpleName(), "Configuring notification channels");
        
        NotificationChannel controls = new NotificationChannel(
            ID_CONTROLS,
            context.getText(R.string.notification_channel_controls),
            /*
             * The notification helps keep the user aware that the display contents are sideways
             * while also allowing them to easily control the app. However, using an importance of
             * "default" or higher causes a sound to be played, which isn't appropriate for this
             * notification, so I've chosen the next best option, "low".
             */
            NotificationManager.IMPORTANCE_LOW
        );
        controls.setShowBadge(false); // This isn't needed, but it makes it clearer to the user that the service notification won't show as a badge.
        controls.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // So when the user unlocks the phone, they can see if they've left the app on
        controls.enableVibration(false); // Just to make sure this never triggers a vibration, even if the user configures it to do so, since the notification is a response to user interaction
        controls.setVibrationPattern(null); // Just to make sure this never triggers a vibration, even if the user configures it to do so, since the notification is a response to user interaction
        controls.setSound(null, new AudioAttributes.Builder().build()); // Just to make sure this never triggers a sound, even if the user configures it to do so, since the notification is a response to user interaction

        NotificationChannel upgrades = new NotificationChannel(
            ID_UPGRADES,
            context.getText(R.string.notification_channel_upgrades),
            // Don't want to interrupt the user for release notes; they're not important.
            NotificationManager.IMPORTANCE_LOW
        );
        upgrades.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        
        NotificationChannel errors = new NotificationChannel(
            ID_ERRORS,
            context.getText(R.string.notification_channel_errors),
            // Since the user will probably want to know why auto rotation suddenly stopped working!
            NotificationManager.IMPORTANCE_HIGH
        );
        errors.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        
        NotificationChannel permission = new NotificationChannel(
            ID_PERMISSIONS,
            context.getText(R.string.notification_channel_permissions),
            // Since these only need to be addressed for the incompatible apps function to work
            NotificationManager.IMPORTANCE_DEFAULT
        );
        permission.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        List<NotificationChannel> channels = Arrays.asList(controls, upgrades, errors, permission);
        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannels(channels);
    }
    
}
