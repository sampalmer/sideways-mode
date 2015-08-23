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
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ServiceNotification {

	private final NotificationPriority priority;
	private final NotificationParameters params;
	private NotificationBuilderCompat builder;
	private RemoteViews content;

	public ServiceNotification(NotificationPriority priority, NotificationParameters notificationParameters) {
		this.priority = priority;
		params = notificationParameters;
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build() {
		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		builder = NotificationBuilderCompat.create()
				.setSmallIcon(params.user.iconId())
				.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
				.setContentTitle(StringResource.get(R.string.app_name))
				.setContentText(StringResource.get(params.user.descriptionId()))
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		priority.apply(builder);

		hideTimestamp();
		hideFromLockScreen();

		Notification notification = builder.build();
		addActions(notification);

		return notification;
	}

	private void addActions(Notification notification) {
		content = new RemoteViews(Process.context().getPackageName(), R.layout.notification);
		content.removeAllViews(R.id.notification_main_column);
		content.addView(R.id.notification_main_column, notification.contentView);

		configureButton(R.id.stop_button, RotationService.ACTION_STOP, R.drawable.ic_action_power, R.string.action_stop);
		configureButton(R.id.toggle_mode_button, RotationService.ACTION_TOGGLE, params.mode.icon(params.overridden), params.mode.toggleToOtherModeAResourceId());
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
}
