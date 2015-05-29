package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.components.ToggleRotationLockReceiver;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationTimestamp;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ServiceNotification {

	private final Mode mode;
	private final NotificationPriority priority;
	private final boolean overridden;
	private final Orientation user;
	private NotificationBuilderCompat builder;

	public ServiceNotification(NotificationPriority priority, Mode mode, boolean overridden, Orientation user) {
		this.priority = priority;
		this.mode = mode;
		this.overridden = overridden;
		this.user = user;
	}

	/**
	 * Reduces chance of being killed
	 */
	public Notification build() {
		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		builder = NotificationBuilderCompat.create()
				.setSmallIcon(mode.iconResourceId(overridden))
				.setContentIntent(new Intents(ToggleRotationLockReceiver.class).makePending())
				.setContentTitle(buildTitle())
				.setContentText(StringResource.get(mode.actionDescriptionResourceId()))
				.setPriority(priority.androidPriority());

		if (overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		if (priority == NotificationPriority.HIGH)
			moveToTop();

		hideTimestamp();
		hideFromLockScreen();

		builder.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		return builder.build();
	}

	private CharSequence buildTitle() {
		return StringResource.format(R.string.notification_summary, mode.autoRotationStateResourceId(), userStateResourceId());
	}

	private int userStateResourceId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
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

	private void moveToTop() {
		builder.setWhen(NotificationTimestamp.highest());
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}
}
