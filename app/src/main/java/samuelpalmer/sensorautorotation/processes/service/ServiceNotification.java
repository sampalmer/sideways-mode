package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import androidx.core.content.ContextCompat;
import android.util.Log;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationButtons;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationStyles;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

class ServiceNotification {
	
	private final Context context;
	private final NotificationBuilderCompat builder;
	
	//Cached for RAM & CPU efficiency
	private final int actionButtonColour;
	private final int unselectedButtonBackground;
	
	public ServiceNotification(Context context) {
		this.context = context;
		
		PendingIntent dashboardIntent = PendingIntent.getActivity(
		this.context,
			PendingIntentIds.MAIN_NOTIFICATION_CLICK,
			new Intent(this.context, DashboardActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
			PendingIntentIds.PENDING_INTENT_FLAGS);
		
		//Caching and re-using the builder prevents the notification from disappearing and
		// reappearing each time it is updated on my Sony Xperia M (Android 4.3).
		builder = NotificationBuilderFactory.create(this.context)
			.setContentIntent(dashboardIntent)
 			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET) //To decrease likelihood of showing on lock screen
			.setChannelId(NotificationChannels.ID_CONTROLS)
			.setOnlyAlertOnce(true) // Don't want to alert the user each time we update the state of the service notification
			.setPriority(NotificationBuilderCompat.PRIORITY_DEFAULT)
		;
		
		hideTimestamp();
		
		NotificationStyles styles = new NotificationStyles(this.context, R.drawable.action_turn_off, ContextCompat.getColor(this.context, R.color.primary_500));
		actionButtonColour = styles.getActionButtonColour();
		unselectedButtonBackground = styles.getUnselectedButtonBackground();
		
		Log.i(ServiceNotification.class.getSimpleName(), "Action button colour: " + Integer.toHexString(actionButtonColour));
	}

	public Notification build(ApplicationStatus.RunningState runningState) {
		builder
			.setSmallIcon(runningState.userSide.iconId)
			.setProgress(0, 0, runningState.waitingForSensor);

		int statusTextResource =
			runningState.waitingForSensor
			?
			R.string.status_waiting_sensor
			:
			runningState.userSide.descriptionId;
		
		CharSequence statusText = context.getText(statusTextResource);

		if (VERSION.SDK_INT >= 24) {
			//In Android N, the app name is already shown in the notification's header section, so we can skip it here.
			builder.setContentTitle(statusText);
		}
		else {
			builder.setContentTitle(context.getText(R.string.app_name_full));
			builder.setContentText(statusText);
		}
		
		applyButtons(runningState.userSide);

		return builder.build();
	}
	
	private void applyButtons(UserSide userSide) {
		builder.setCustomContentView(null); //So the pre-buttons notifification doesn't contain the buttons from last time around.

		NotificationButtons notificationButtons = new NotificationButtons(
		context,
			R.layout.notification,
			builder.createContentView(),
			actionButtonColour,
			unselectedButtonBackground
		);
		
		notificationButtons
			.configureButton(
				R.id.notification_left_side_button,
				R.drawable.app_left_side,
				PendingIntentIds.MAIN_NOTIFICATION_LEFT_SIDE,
				// Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
				userSide.equals(UserSide.LEFT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_LEFT_SIDE).asIntent(context),
				context.getText(R.string.user_rotation_left_side),
				userSide.equals(UserSide.LEFT))
			.configureButton(
				R.id.notification_turn_off_button,
				R.drawable.action_turn_off,
				PendingIntentIds.MAIN_NOTIFICATION_TURN_OFF,
				new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).asIntent(context),
				context.getText(R.string.action_turn_off),
				false)
			.configureButton(
				R.id.notification_right_side_button,
				R.drawable.app_right_side,
				PendingIntentIds.MAIN_NOTIFICATION_RIGHT_SIDE,
				// Android 4.0 doesn't support using a "null" pending intent, so we need to use a no-op one instead.
				userSide.equals(UserSide.RIGHT) ? new Intent(context, DummyReceiver.class) : new ApplicationCommand(ApplicationCommand.ACTION_RIGHT_SIDE).asIntent(context),
				context.getText(R.string.user_rotation_right_side),
				userSide.equals(UserSide.RIGHT))
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
