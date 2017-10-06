package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.notifications.NotificationButtons;
import samuelpalmer.common.notifications.NotificationStyles;
import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.ui.preferences.MainSettingsActivity;

class ServiceNotification {

	private final NotificationBuilderCompat builder;
	private final int actionButtonColour; //Cached for RAM & CPU efficiency

	public ServiceNotification() {
		PendingIntent settingsActivityIntent = PendingIntent.getActivity(
			CurrentProcess.context(),
			PendingIntentIds.MAIN_NOTIFICATION_CLICK,
			new Intent(CurrentProcess.context(), MainSettingsActivity.class),
			PendingIntentIds.PENDING_INTENT_FLAGS);
		
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderFactory.create()
			.setContentIntent(settingsActivityIntent)
 			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
			.setChannelId(NotificationChannels.ID_CONTROLS)
			.setPriority(NotificationBuilderCompat.PRIORITY_DEFAULT)
		;
		
		hideTimestamp();
		
		NotificationStyles styles = new NotificationStyles(CurrentProcess.context(), R.drawable.action_change_side, ContextCompat.getColor(CurrentProcess.context(), R.color.primary_500));
		actionButtonColour = styles.getActionButtonColour();

		Log.i(ServiceNotification.class.getSimpleName(), "Action button colour: " + Integer.toHexString(actionButtonColour));
	}

	public Notification build(ApplicationStatus applicationStatus) {
		builder
			.setSmallIcon(applicationStatus.user.iconId)
			.setProgress(0, 0, applicationStatus.waitingForSensor);

		int statusTextResource =
			applicationStatus.waitingForSensor
			?
			R.string.status_waiting_sensor
			:
			applicationStatus.user.descriptionId;
		
		CharSequence statusText = CurrentProcess.context().getText(statusTextResource);

		if (VERSION.SDK_INT >= 24) {
			//In Android N, the app name is already shown in the notification's header section, so we can skip it here.
			builder.setContentTitle(statusText);
		}
		else {
			builder.setContentTitle(CurrentProcess.context().getText(R.string.app_name));
			builder.setContentText(statusText);
		}
		
		applyButtons();

		return builder.build();
	}
	
	private void applyButtons() {
		builder.setCustomContentView(null); //So the pre-buttons notifification doesn't contain the buttons from last time around.

		NotificationButtons notificationButtons = new NotificationButtons(
			CurrentProcess.context(),
			R.layout.notification,
			builder.createContentView(),
			actionButtonColour
		);

		notificationButtons
			.configureButton(
				R.id.change_side_button,
				R.drawable.action_change_side,
				new ApplicationCommand(ApplicationCommand.ACTION_CHANGE_SIDE).asPendingIntent(CurrentProcess.context(), PendingIntentIds.MAIN_NOTIFICATION_CHANGE_SIDE),
				CurrentProcess.context().getText(R.string.action_change_side))
			.configureButton(
				R.id.turn_off_button,
				R.drawable.action_turn_off,
				new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).asPendingIntent(CurrentProcess.context(), PendingIntentIds.MAIN_NOTIFICATION_TURN_OFF),
				CurrentProcess.context().getText(R.string.action_turn_off))
		;

		builder.setCustomContentView(notificationButtons.getContentView());
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

}
