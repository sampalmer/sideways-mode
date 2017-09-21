package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

class ServiceNotification {

	private final NotificationBuilderCompat builder;

	public ServiceNotification() {
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderFactory.create()
			.setSmallIcon(R.drawable.notification_app)
			.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent(PendingIntentIds.MAIN_NOTIFICATION_CLICK))
 			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
			.setChannelId(NotificationChannels.ID_CONTROLS)
			.setPriority(NotificationBuilderCompat.PRIORITY_LOW)
			.addAction(
				R.drawable.action_change_side,
				StringResource.get(R.string.action_change_side),
				ApplicationInstanceReceiver.getPendingIntent(new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_CHANGE_SIDE), PendingIntentIds.MAIN_NOTIFICATION_CHANGE_SIDE)
			)
			.addAction(
				R.drawable.action_turn_off,
				StringResource.get(R.string.action_turn_off),
				ApplicationInstanceReceiver.getPendingIntent(new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_TURN_OFF), PendingIntentIds.MAIN_NOTIFICATION_TURN_OFF)
			)
		;
		
		hideTimestamp();
	}

	public Notification build(ApplicationStatus applicationStatus) {
		builder.setProgress(0, 0, applicationStatus.waitingForSensor);

		int statusTextResource =
			applicationStatus.waitingForSensor
			?
			R.string.status_waiting_sensor
			:
			applicationStatus.user.descriptionId();
		
		CharSequence statusText = StringResource.get(statusTextResource);

		if (VERSION.SDK_INT >= 24) {
			//In Android N, the app name is already shown in the notification's header section, so we can skip it here.
			builder.setContentTitle(statusText);
		}
		else {
			builder.setContentTitle(StringResource.get(R.string.app_name));
			builder.setContentText(statusText);
		}
		
		return builder.build();
	}
	
	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

}
