package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;
import android.widget.RemoteViews;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.LockScreenStatus;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ServiceNotification {

	private final NotificationPriority priority;
	private final NotificationParameters params;
	private final NotificationStyles styles;
	private NotificationBuilderCompat builder;
	private RemoteViews content;

	public ServiceNotification(NotificationPriority priority, NotificationParameters notificationParameters) {
		this.priority = priority;
		params = notificationParameters;
		styles = new NotificationStyles();
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build() {
		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		builder = NotificationBuilderCompat.create()
				.setSmallIcon(params.mode.iconResourceId(params.overridden))
				.setContentIntent(RotationServiceReceiver.getPendingIntent(RotationService.ACTION_TOGGLE))
				.setContentTitle(params.summary)
				.setContentText(StringResource.get(params.mode.actionDescriptionResourceId()))
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		if (params.overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

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

		configureButton(R.id.rotate_left_button, RotationService.ACTION_ROTATE_LEFT);
		configureButton(R.id.rotate_right_button, RotationService.ACTION_ROTATE_RIGHT);

		notification.contentView = content;
	}

	private void configureButton(int id, String action) {
		content.setOnClickPendingIntent(id, RotationServiceReceiver.getPendingIntent(action));
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

			if (LockScreenStatus.isActive())
				builder.setPriority(NotificationBuilderCompat.PRIORITY_MIN);
		}
	}
}
