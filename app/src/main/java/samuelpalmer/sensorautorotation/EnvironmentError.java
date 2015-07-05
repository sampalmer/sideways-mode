package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;

import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class EnvironmentError {

	public static void report(int messageResourceId, Object... messageArguments) {
		CharSequence messageEndingWithFullStop = StringResource.format(messageResourceId, messageArguments);
		CharSequence formattedMessage = StringResource.format(R.string.environment_error_message, messageEndingWithFullStop, getAppName());
		show(formattedMessage);
		new ApplicationSettings().enabled().setAndCommit(false);
	}

	private static void show(CharSequence message) {
		Notification notification = NotificationBuilderCompat.create()
				.setSmallIcon(R.drawable.ic_error)
				.setContentIntent(makeShowMessage(message))
				.setContentTitle(getAppName())
				.setContentText(message)
				.setAutoCancel(true)
				.setTicker(message)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
				.build();

		SystemServices.notificationManager().notify(NotificationIds.ERROR, notification);
	}

	private static CharSequence getAppName() {
		return StringResource.get(R.string.app_name);
	}

	private static PendingIntent makeShowMessage(CharSequence message) {
		return DialogActivity.makePendingIntent(getAppName(), message);
	}

	public static void clear() {
		SystemServices.notificationManager().cancel(NotificationIds.ERROR);
	}

}