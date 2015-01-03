package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.sensorautorotation.components.ErrorDialogActivity;

public class EnvironmentErrorNotifier{

	private Context context;

	public EnvironmentErrorNotifier(Context context) {
		this.context = context;
	}

	public void handle(String message) {
		show(message + " " + getAppName() + " will be disabled.");
		new ServiceController(context).ensureStopped();
		new Settings(context).enabled().set(false);
	}

	private void show(String message) {
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(makeShowMessage(message))
				.setContentTitle(getAppName())
				.setContentText(message)
				.setAutoCancel(true)
				.setTicker(message)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(0, notification);
	}

	private String getAppName() {
		return context.getString(R.string.app_name);
	}

	private PendingIntent makeShowMessage(String message) {
		Intents intents = new Intents(context);
		Intent intent = intents.makeIntent(ErrorDialogActivity.class)
				.putExtra(ErrorDialogActivity.MESSAGE_INTENT_EXTRA_NAME, message);
		return intents.makePending(intent);
	}

}