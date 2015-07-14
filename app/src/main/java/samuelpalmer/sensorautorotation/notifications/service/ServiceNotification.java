package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.processes.service.LockScreenStatus;
import samuelpalmer.sensorautorotation.processes.service.ToggleRotationLockReceiver;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ServiceNotification {

	private final NotificationPriority priority;
	private final NotificationParameters params;
	private NotificationBuilderCompat builder;

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
				.setSmallIcon(params.mode.iconResourceId(params.overridden))
				.setContentIntent(ComponentType.create(ToggleRotationLockReceiver.class).pendingIntent())
				.setContentTitle(params.summary)
				.setContentText(StringResource.get(params.mode.actionDescriptionResourceId()))
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		if (params.overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		priority.apply(builder);

		hideTimestamp();
		hideFromLockScreen();

		return builder.build();
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
