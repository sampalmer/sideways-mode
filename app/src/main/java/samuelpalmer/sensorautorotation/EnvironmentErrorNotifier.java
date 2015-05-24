package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.DialogActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class EnvironmentErrorNotifier{

	public void handle(int messageResourceId, Object... messageArguments) {
		String messageEndingWithFullStop = ThisApplication.context().getString(messageResourceId, messageArguments);
		String formattedMessage = ThisApplication.context().getString(R.string.environment_error_message, messageEndingWithFullStop, getAppName());
		show(formattedMessage);
		new ApplicationSettings().enabled().setAndCommit(false);
	}

	private void show(String message) {
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

		notificationManager().notify(NotificationIds.ERROR, notification);
	}

	private String getAppName() {
		return ThisApplication.context().getString(R.string.app_name);
	}

	private PendingIntent makeShowMessage(String message) {
		Intents intents = new Intents(DialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, getAppName());
		return intents.makePending(intent);
	}

	public void clear() {
		notificationManager().cancel(NotificationIds.ERROR);
	}

	private NotificationManager notificationManager() {
		return (NotificationManager) ThisApplication.context().getSystemService(Activity.NOTIFICATION_SERVICE);
	}
}