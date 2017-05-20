package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.notifications.NotificationButtons;
import samuelpalmer.common.notifications.NotificationStyles;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceCommand;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;

class ServiceNotification {

	private final NotificationBuilderCompat builder;
	private final int actionButtonColour; //Cached for RAM & CPU efficiency

	public ServiceNotification() {
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderFactory.create()
			.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent(PendingIntentIds.MAIN_NOTIFICATION_CLICK))
 			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
			.setChannel(NotificationChannels.ID_DASHBOARD)
		;
		
		NotificationStyles styles = new NotificationStyles(CurrentProcess.context(), R.drawable.action_rotation, ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500));
		actionButtonColour = styles.getActionButtonColour();

		Log.i(ServiceNotification.class.getSimpleName(), "Action button colour: " + Integer.toHexString(actionButtonColour));
	}

	public Notification build(ServiceStatus serviceStatus) {
		builder
			.setSmallIcon(serviceStatus.getIcon())
			.setProgress(0, 0, serviceStatus.waitingForSensor)
		;

		int statusTextResource =
			serviceStatus.waitingForSensor
			?
			R.string.status_waiting_sensor
			:
			serviceStatus.user.descriptionId();
		
		CharSequence statusText = StringResource.get(statusTextResource);

		if (VERSION.SDK_INT >= 24) {
			//In Android N, the app name is already shown in the notification's header section, so we can skip it here.
			builder.setContentTitle(statusText);
		}
		else {
			builder.setContentTitle(StringResource.get(R.string.app_name));
			builder.setContentText(statusText);
		}

		serviceStatus.priority.apply(builder);
		hideTimestamp();

		applyButtons(serviceStatus);

		return builder.build();
	}
	
	private void applyButtons(ServiceStatus serviceStatus) {
		builder.setCustomContentView(null); //So the pre-buttons notifification doesn't contain the buttons from last time around.

		NotificationButtons notificationButtons = new NotificationButtons(
			CurrentProcess.context(),
			R.layout.notification,
			builder.createContentView(),
			actionButtonColour
		);

		RotationServiceCommand toggleCommand = new RotationServiceCommand(RotationServiceCommand.ACTION_TOGGLE);
		toggleCommand.showToast = true;

		notificationButtons
			.configureButton(
				R.id.rotate_left_button,
				R.drawable.action_rotate_left,
				RotationServiceReceiver.getPendingIntent(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_LEFT), PendingIntentIds.MAIN_NOTIFICATION_ROTATE_LEFT),
				StringResource.get(R.string.action_rotate_left))
			.configureButton(
				R.id.toggle_mode_button,
				serviceStatus.mode.actionIcon(),
				RotationServiceReceiver.getPendingIntent(toggleCommand, PendingIntentIds.MAIN_NOTIFICATION_TOGGLE),
				StringResource.get(serviceStatus.mode.toggleToOtherModeResourceId()))
			.configureButton(
				R.id.rotate_right_button,
				R.drawable.action_rotate_right,
				RotationServiceReceiver.getPendingIntent(new RotationServiceCommand(RotationServiceCommand.ACTION_ROTATE_RIGHT), PendingIntentIds.MAIN_NOTIFICATION_ROTATE_RIGHT),
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
