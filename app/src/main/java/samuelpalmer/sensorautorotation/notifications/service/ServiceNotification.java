package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.notifications.NotificationButtons;
import samuelpalmer.common.notifications.NotificationStyles;
import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

class ServiceNotification {

	private final NotificationBuilderCompat builder;
	private final int actionButtonColour; //Cached for RAM & CPU efficiency

	public ServiceNotification() {
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderFactory.create()
			.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
 			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
		;

		NotificationStyles styles = new NotificationStyles(Process.context(), R.drawable.action_rotation, ColourGetterCompat.getColor(Process.context(), R.color.primary_500));
		actionButtonColour = styles.getActionButtonColour();
	}

	public Notification build(ServiceStatus serviceStatus) {
		builder.setSmallIcon(serviceStatus.overridden ? serviceStatus.user.iconId() : serviceStatus.mode.notificationIcon());

		if (VERSION.SDK_INT >= 24) {
			//In Android N, the app name is already shown in the notification's header section, so we can skip it here.
			builder.setContentTitle(StringResource.get(serviceStatus.user.descriptionId()));
		}
		else {
			builder.setContentTitle(StringResource.get(R.string.app_name));
			builder.setContentText(StringResource.get(serviceStatus.user.descriptionId()));
		}

		serviceStatus.priority.apply(builder);
		hideTimestamp();

		applyButtons(serviceStatus);

		return builder.build();
	}

	private void applyButtons(ServiceStatus serviceStatus) {
		builder.setCustomContentView(null); //So the pre-buttons notifification doesn't contain the buttons from last time around.

		NotificationButtons notificationButtons = new NotificationButtons(
			Process.context(),
			R.layout.notification,
			builder.createContentView(),
			actionButtonColour
		);

		notificationButtons
			.configureButton(
				R.id.rotate_left_button,
				R.drawable.action_rotate_left,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_LEFT),
				StringResource.get(R.string.action_rotate_left))
			.configureButton(
				R.id.toggle_mode_button,
				serviceStatus.mode.actionIcon(),
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE),
				StringResource.get(serviceStatus.mode.toggleToOtherModeResourceId()))
			.configureButton(
				R.id.rotate_right_button,
				R.drawable.action_rotate_right,
				RotationServiceReceiver.getPendingIntent(RotationService.ACTION_ROTATE_RIGHT),
				StringResource.get(R.string.action_rotate_right));

		builder.setCustomContentView(notificationButtons.getContentView());
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

}
