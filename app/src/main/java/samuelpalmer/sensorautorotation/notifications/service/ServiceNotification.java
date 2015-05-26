package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.os.Build;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.components.ToggleRotationLockActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;

public class ServiceNotification {

	private final Mode mode;
	private final boolean showAtTop;
	private final boolean overridden;
	private final Orientation user;
	private NotificationBuilderCompat builder;

	public ServiceNotification(boolean showAtTop, Mode mode, boolean overridden, Orientation user) {
		this.showAtTop = showAtTop;
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
				.setContentIntent(new Intents(ToggleRotationLockActivity.class).makePending())
				.setContentTitle(buildTitle())
				.setContentText(StringResource.get(mode.actionDescriptionResourceId()));

		if (overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		if (showAtTop)
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
		builder.setPriority(NotificationBuilderCompat.PRIORITY_MAX);
		builder.setWhen(bestWhen());
	}

	private static long bestWhen() {
		long result = System.currentTimeMillis();

		if (Build.VERSION.SDK_INT < 21) //Because Android 5 actually ignores future 'when' values ordering and even doesn't update the notification time when they're future
			result += millisecondsFromYears(10L); //Can't go too high (eg Integer.MAX_VALUE) since that seems to cause the opposite effect of what we want (at least in Android 4.3)

		return result;
	}

	private static long millisecondsFromYears(long years) {
		return years * 365L * 24L * 60L * 60L * 1000L;
	}

	private void hideTimestamp() {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}
}
