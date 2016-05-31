package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
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

		//This is useful when checking log output from Cloud Test Lab on Google Play Store.
		Log.i(EnvironmentError.class.getSimpleName(), instance.summary + ". " + instance.message);

		instance.show();
		new ServiceSynchroniser(ApplicationSettings.remote()).disableService();
	}

	private EnvironmentError(){}

	private void show() {
		NotificationBuilderCompat builder = NotificationBuilderFactory.create()
			.setSmallIcon(R.drawable.error)
			.setContentIntent(DialogActivity.makePendingIntent(summary, message))
			.setContentTitle(summary)
			.setContentText(message)
			.setAutoCancel(true)
			.setTicker(message)
			.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
			.setCategory(NotificationBuilderCompat.CATEGORY_ERROR);

		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP)
			// Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
			// the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
			builder.setVibrate(new long[]{});

		Notification notification = builder.build();

		SystemServices.notificationManager().notify(NotificationIds.ERROR, notification);
	}

	public static void clear() {
		SystemServices.notificationManager().cancel(NotificationIds.ERROR);
	}

}