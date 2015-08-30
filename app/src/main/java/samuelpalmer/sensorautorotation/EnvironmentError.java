package samuelpalmer.sensorautorotation;

import android.app.Notification;

import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class EnvironmentError {

	private CharSequence summary;
	private CharSequence message;

	public static void report(int summaryResourceId, int messageResourceId, Object... messageArguments) {
		EnvironmentError instance = new EnvironmentError();
		instance.summary = StringResource.get(summaryResourceId);
		CharSequence messageEndingWithFullStop = StringResource.format(messageResourceId, messageArguments);
		instance.message = StringResource.format(R.string.environment_error_message, messageEndingWithFullStop, R.string.app_name);

		instance.show();
		new ServiceSynchroniser(ApplicationSettings.remote()).disableService();
	}

	private EnvironmentError(){}

	private void show() {
		Notification notification = NotificationBuilderCompat.create()
				.setSmallIcon(R.drawable.ic_error)
				.setContentIntent(DialogActivity.makePendingIntent(summary, message))
				.setContentTitle(summary)
				.setContentText(message)
				.setAutoCancel(true)
				.setTicker(message)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
				.build();

		SystemServices.notificationManager().notify(NotificationIds.ERROR, notification);
	}

	public static void clear() {
		SystemServices.notificationManager().cancel(NotificationIds.ERROR);
	}

}