package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import samuelpalmer.sensorautorotation.components.ErrorDialogActivity;

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
		Notification.Builder builder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_error)
				.setContentIntent(makeShowMessage(message))
				.setContentTitle(getAppName())
				.setContentText(message)
				.setAutoCancel(true)
				.setTicker(message);

		if (Build.VERSION.SDK_INT >= 16)
			builder.setPriority(Notification.PRIORITY_HIGH);

		if (Build.VERSION.SDK_INT >= 21)
			builder.setCategory(Notification.CATEGORY_ERROR);

		Notification notification = build(builder);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(NotificationIds.ERROR, notification);
	}

	private Notification build(Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= 16)
			return builder.build();
		else
			//noinspection deprecation
			return builder.getNotification();
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