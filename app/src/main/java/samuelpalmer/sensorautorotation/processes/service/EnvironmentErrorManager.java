package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class EnvironmentErrorManager {
	
	public static void report(EnvironmentError error) {
		//This is useful when checking log output from Cloud Test Lab on Google Play Store.
		Log.i(EnvironmentError.class.getSimpleName(), error.getSummary() + ". " + error.getClass().getSimpleName() + ": " + error.logMessage());
		
		postNotification(error);
		
		ServiceSettings.instance().lastErrorJson().set(error.toJson());
		
		new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(CurrentProcess.context());
	}
	
	public static void clear() {
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).cancel(NotificationIds.ERROR);
		ServiceSettings.instance().lastErrorJson().remove();
	}
	
	private static void postNotification(EnvironmentError error) {
		NotificationBuilderCompat builder = NotificationBuilderFactory.create()
			.setSmallIcon(R.drawable.notification_error) //TODO: Maybe just use the app's normal icon so it's clear which app posted this notification
			.setContentIntent(error.showError())
			.setContentTitle(error.getSummary())
			.setContentText(CurrentProcess.context().getText(R.string.app_disabled_view_error))
			.setAutoCancel(true)
			.setDeleteIntent(new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).asPendingIntent(CurrentProcess.context(), PendingIntentIds.ENVIRONMENT_ERROR_NOTIFICATION_CLEAR))
			.setTicker(error.getSummary()) //Helps ensure the user actually notices that the error occurred. Only works on Android 4.
			.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
			.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
			.setChannelId(NotificationChannels.ID_ERRORS);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
			// Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
			// the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
			builder.setVibrate(new long[]{});
		
		Notification notification = builder.build();
		
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).notify(NotificationIds.ERROR, notification);
	}
	
}
