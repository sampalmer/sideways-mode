package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.sensorautorotation.components.ErrorDialogActivity;

public class UserErrorHandler {

	private Context context;

	public UserErrorHandler(Context context) {
		this.context = context;
	}

	public void handle(EnvironmentException error) {
		show(error.getMessage() + " " + getAppName() + " will be disabled.");
		new ServiceController(context).ensureStopped();
		new Settings(context).setEnabled(false);
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
