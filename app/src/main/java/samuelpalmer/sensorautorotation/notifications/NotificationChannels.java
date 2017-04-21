package samuelpalmer.sensorautorotation.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;

public abstract class NotificationChannels {

	public static final String ID_DASHBOARD = "dashboard";
	public static final String ID_UPGRADES = "upgrades";
	public static final String ID_ERRORS = "errors";
	public static final String ID_PERMISSIONS = "permissions";

	@RequiresApi(api = Build.VERSION_CODES.O)
	public static void setup() {
		Log.i(NotificationChannels.class.getSimpleName(), "Configuring notification channels");

		NotificationChannel dashboard = new NotificationChannel(
				ID_DASHBOARD,
				StringResource.get(R.string.notification_channel_dashboard),

				/*
				 * Deemphasising this notification since it is permanent and shouldn't get in the way of "real" notifications.
				 * Not using "MIN" importance here since that causes the notification to collapse when another notification is above it,
				 * which prevents the controls in the notification from showing by default.
				 */
				NotificationManager.IMPORTANCE_LOW
		);

		NotificationChannel upgrades = new NotificationChannel(
				ID_UPGRADES,
				StringResource.get(R.string.notification_channel_upgrades),
				//Don't want to interrupt the user for release notes; they're not important.
				NotificationManager.IMPORTANCE_LOW
		);

		NotificationChannel errors = new NotificationChannel(
				ID_ERRORS,
				StringResource.get(R.string.notification_channel_errors),
				//Since the user will probably want to know why auto rotation suddenly stopped working!
				NotificationManager.IMPORTANCE_HIGH
		);

		NotificationChannel permission = new NotificationChannel(
				ID_PERMISSIONS,
				StringResource.get(R.string.notification_channel_permissions),
				//Since these only need to be addressed for the overridden apps function to work
				NotificationManager.IMPORTANCE_DEFAULT
		);

		List<NotificationChannel> channels = Arrays.asList(dashboard, upgrades, errors, permission);
		SystemServices.notificationManager().createNotificationChannels(channels);
	}

}
