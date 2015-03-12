package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.ErrorDialogActivity;
import samuelpalmer.sensorautorotation.notification.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notification.NotificationIds;
import samuelpalmer.sensorautorotation.settings.ApplicationSettings;

public class EnvironmentErrorNotifier{

	private final Context context;

	public EnvironmentErrorNotifier(Context context) {
		this.context = context;
	}

	public void handle(String message) {
		show(message + " " + getAppName() + " will be disabled.");
		new ServiceController(context).ensureStopped();
		new ApplicationSettings(context).enabled().setAndCommit(false);
	}

	private void show(String message) {
		Notification notification = NotificationBuilderCompat.create(context)
				.setSmallIcon(R.drawable.ic_error)
				.setContentIntent(makeShowMessage(message))
				.setContentTitle(getAppName())
				.setContentText(message)
				.setAutoCancel(true)
				.setTicker(message)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
				.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(NotificationIds.ERROR, notification);
	}

	private String getAppName() {
		return context.getString(R.string.app_name);
	}

	private PendingIntent makeShowMessage(String message) {
		Intents intents = new Intents(context, ErrorDialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(ErrorDialogActivity.MESSAGE_INTENT_EXTRA_NAME, message);
		return intents.makePending(intent);
	}

}