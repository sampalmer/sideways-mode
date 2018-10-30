package samuelpalmer.sensorautorotation.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.R;

public abstract class NotificationChannels {

	public static final String ID_CONTROLS = "controls";
	public static final String ID_UPGRADES = "upgrades";
	public static final String ID_ERRORS = "errors";
	public static final String ID_PERMISSIONS = "permissions";
	public static final String ID_TIPS = "tips";

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
		controls.setLockscreenVisibility(Notification.VISIBILITY_SECRET); // Since this notification isn't really relevant on a typical lock screen

		NotificationChannel upgrades = new NotificationChannel(
			ID_UPGRADES,
			context.getText(R.string.notification_channel_upgrades),
			//Don't want to interrupt the user for release notes; they're not important.
			NotificationManager.IMPORTANCE_LOW
		);
		upgrades.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		
		NotificationChannel errors = new NotificationChannel(
			ID_ERRORS,
			context.getText(R.string.notification_channel_errors),
			//Since the user will probably want to know why auto rotation suddenly stopped working!
			NotificationManager.IMPORTANCE_HIGH
		);
		errors.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		
		NotificationChannel permission = new NotificationChannel(
			ID_PERMISSIONS,
			context.getText(R.string.notification_channel_permissions),
			//Since these only need to be addressed for the incompatible apps function to work
			NotificationManager.IMPORTANCE_DEFAULT
		);
		permission.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

		NotificationChannel tips = new NotificationChannel(
			ID_TIPS,
            context.getText(R.string.notification_channel_tips),
            //We might need to interrupt the user to point out important stuff such as:
            // - That they must turn off the app before uninstalling
            // - That the app they're currently using might need to be added to the incompatible apps list
            NotificationManager.IMPORTANCE_HIGH
		);
		permission.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

		List<NotificationChannel> channels = Arrays.asList(controls, upgrades, errors, permission, tips);
		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannels(channels);
	}
	
}
