package samuelpalmer.sensorautorotation.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
			 * Using normal importance since the user needs to:
			 * 1. Know that everything will be rotated sideways
			 * 2. Have easy access to the controls in the notification
			 */
			NotificationManager.IMPORTANCE_DEFAULT
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

		List<NotificationChannel> channels = Arrays.asList(controls, upgrades, errors, permission);
		ContextCompat.getSystemService(context, NotificationManager.class).createNotificationChannels(channels);
	}
	
}
