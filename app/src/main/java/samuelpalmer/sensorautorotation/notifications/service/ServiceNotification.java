package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.os.Build.VERSION;
import android.widget.RemoteViews;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.LockScreenMonitor;
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
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		params.priority.apply(builder);

		hideTimestamp();
		hideFromLockScreen();

		Notification notification = builder.build();
		addButtons(notification);

		return notification;
	}

	private NotificationBuilderCompat builder;

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

	private void hideFromLockScreen() {
		//Notifications only show on lock screen as of Android 5
		if (Build.VERSION.SDK_INT >= 21) {
			//This seems to be the best we've got: http://stackoverflow.com/questions/27035202/how-to-suppress-notification-on-lock-screen-in-android-5-lollipop-but-let-it-i
			builder.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET);

			if (LockScreenMonitor.isOnLockScreen())
				builder.setPriority(NotificationBuilderCompat.PRIORITY_MIN);
		}
	}

	private void addButtons(Notification notification) {
		content = new RemoteViews(Process.context().getPackageName(), R.layout.notification);
		content.removeAllViews(R.id.notification_main_column);
		content.addView(R.id.notification_main_column, notification.contentView);

		configureButton(R.id.toggle_mode_button, RotationService.ACTION_TOGGLE, params.mode.actionIcon(), params.mode.toggleToOtherModeAResourceId());
		configureButton(R.id.rotate_left_button, RotationService.ACTION_ROTATE_LEFT, R.drawable.ic_action_rotate_left, R.string.action_rotate_left);
		configureButton(R.id.rotate_right_button, RotationService.ACTION_ROTATE_RIGHT, R.drawable.ic_action_rotate_right, R.string.action_rotate_right);

		notification.contentView = content;
	}

	private void configureButton(int id, String action, int icon, int contentDescriptionId) {
		content.setOnClickPendingIntent(id, RotationServiceReceiver.getPendingIntent(action));

		if (VERSION.SDK_INT >= 15)
			content.setContentDescription(id, StringResource.get(contentDescriptionId));

		content.setImageViewResource(id, icon);
		content.setInt(id, "setColorFilter", NotificationStyles.getActionButtonColour());
	}

	private RemoteViews content;
}
