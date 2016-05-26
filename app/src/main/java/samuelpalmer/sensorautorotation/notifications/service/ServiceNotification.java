package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;

import samuelpalmer.common.notifications.NotificationButtons;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

public class ServiceNotification {

	private final NotificationBuilderCompat builder;

	public ServiceNotification() {
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderCompat.create()
			.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
			.setContentTitle(StringResource.get(R.string.app_name))
			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
		;
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build(NotificationParameters params) {
		builder.setSmallIcon(params.overridden ? params.user.iconId() : params.mode.notificationIcon())
			.setContentText(StringResource.get(params.user.descriptionId()));
		params.priority.apply(builder);
		hideTimestamp();

		Notification notification = builder.build();

		NotificationButtons notificationButtons = new NotificationButtons(Process.context(), R.layout.notification);

		notificationButtons
			.configureButton(
				R.id.rotate_left_button,
				R.drawable.action_rotate_left,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_LEFT),
				StringResource.get(R.string.action_rotate_left))
			.configureButton(
				R.id.toggle_mode_button,
				params.mode.actionIcon(),
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE),
				StringResource.get(params.mode.toggleToOtherModeResourceId()))
			.configureButton(
				R.id.rotate_right_button,
				R.drawable.action_rotate_right,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_RIGHT),
				StringResource.get(R.string.action_rotate_right))
			.applyTo(notification);

		return notification;
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

}
