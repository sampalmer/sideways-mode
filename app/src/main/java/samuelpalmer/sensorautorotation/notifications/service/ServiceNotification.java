package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION;
import android.view.View;

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

	private final NotificationParameters params;

	public ServiceNotification(NotificationParameters notificationParameters) {
		params = notificationParameters;
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build() {
		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		builder = NotificationBuilderCompat.create()
				.setSmallIcon(params.overridden ? params.user.iconId() : params.mode.notificationIcon())
				.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
				.setContentTitle(StringResource.get(R.string.app_name))
				.setContentText(StringResource.get(params.user.descriptionId()))
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET); //To decrease likelihood of showing on lock screen

		params.priority.apply(builder);
		hideTimestamp();

		Notification notification = builder.build();

		NotificationButtons notificationButtons = new NotificationButtons(Process.context());

		if (VERSION.SDK_INT >= 17)
			//The rotate left button should always be on the left side regardless of locale
			notificationButtons.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

		notificationButtons
			.configureButton(
				R.drawable.ic_action_rotate_left,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_LEFT), StringResource.get(R.string.action_rotate_left))
			.configureButton(
				params.mode.actionIcon(),
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE), StringResource.get(params.mode.toggleToOtherModeAResourceId()))
			.configureButton(
				R.drawable.ic_action_rotate_right,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_RIGHT), StringResource.get(R.string.action_rotate_right))
			.applyTo(notification);

		return notification;
	}

	private NotificationBuilderCompat builder;

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

}
