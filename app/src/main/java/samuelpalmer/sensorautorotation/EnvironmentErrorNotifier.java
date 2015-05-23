package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.DialogActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class EnvironmentErrorNotifier{

	private final Context context;

	public EnvironmentErrorNotifier(Context context) {
		this.context = context;
	}

	public void handle(String messageEndingWithFullStop) {
		show(messageEndingWithFullStop + " " + getAppName() + " has been disabled.");
		new ServiceController(context).ensureStopped();
		new ApplicationSettings(context).enabled().setAndCommit(false);
	}

	private void show(String message) {
		Notification notification = NotificationBuilderCompat.create(context)
				.smallIcon.set(R.drawable.ic_error)
				.contentIntent.set(makeShowMessage(message))
				.contentTitle.set(getAppName())
				.contentText.set(message)
				.autoCancel.set(true)
				.ticker.set(message)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
				.build();

		notificationManager().notify(NotificationIds.ERROR, notification);
	}

	private String getAppName() {
		return context.getString(R.string.app_name);
	}

	private PendingIntent makeShowMessage(String message) {
		Intents intents = new Intents(context, DialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, R.string.app_name);
		return intents.makePending(intent);
	}

	public void clear() {
		notificationManager().cancel(NotificationIds.ERROR);
	}

	private NotificationManager notificationManager() {
		return (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
	}
}