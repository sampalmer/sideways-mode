package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.components.ToggleRotationLockActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceNotification {

	private final Service service;
	private final ApplicationSettings settings;
	private final OrientationCalculator calculator;
	private final Screen screen;
	private Mode currentMode;

	public ServiceNotification(Service service, ApplicationSettings settings, OrientationCalculator calculator, Screen screen) {
		this.service = service;
		this.settings = settings;
		this.calculator = calculator;
		this.screen = screen;
	}

	/**
	 * Reduces chance of being killed
	 */
	public void set() {
		currentMode = calculateMode();
		boolean overridden = overridden();

		Intents intents = new Intents(ToggleRotationLockActivity.class);

		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		NotificationBuilderCompat builder = NotificationBuilderCompat.create()
				.setSmallIcon(currentMode.iconResourceId(overridden))
				.setContentIntent(intents.makePending())
				.setContentTitle(buildTitle())
				.setContentText(StringResource.get(currentMode.actionDescriptionResourceId()));

		if (overridden)
			builder.setContentInfo(StringResource.get(R.string.notification_overridden));

		if (settings.showNotificationAtTop().get())
			moveToTop(builder);

		hideTimestamp(builder);
		hideFromLockScreen(builder);

		builder.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);

		Notification notification = builder.build();

		service.startForeground(NotificationIds.SERVICE, notification);
	}

	public Mode calculateMode() {
		return calculator.autoRotate() ? new AutoRotationMode() : new LockedMode(calculator, screen);
	}

	private CharSequence buildTitle() {
		return StringResource.format(R.string.notification_summary, currentMode.autoRotationStateResourceId(), userStateResourceId());
	}

	private int userStateResourceId() {
		switch (calculator.user().getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

	private boolean overridden() {
		Orientation requested = new SystemSettings().userRotation.get();
		Orientation actual = screen.realOrientation();

		return !requested.equals(actual);
	}

	private void hideFromLockScreen(NotificationBuilderCompat builder) {
		//Notifications only show on lock screen as of Android 5
		if (Build.VERSION.SDK_INT >= 21) {
			//This seems to be the best we've got: http://stackoverflow.com/questions/27035202/how-to-suppress-notification-on-lock-screen-in-android-5-lollipop-but-let-it-i
			builder.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET);

			if (LockScreenStatus.isActive())
				builder.setPriority(NotificationBuilderCompat.PRIORITY_MIN);
		}
	}

	private void moveToTop(NotificationBuilderCompat builder) {
		builder.setPriority(NotificationBuilderCompat.PRIORITY_MAX);
		builder.setWhen(bestWhen());
	}

	private long bestWhen() {
		long result = System.currentTimeMillis();

		if (Build.VERSION.SDK_INT < 21) //Because Android 5 actually ignores future 'when' values ordering and even doesn't update the notification time when they're future
			result += millisecondsFromYears(10L); //Can't go too high (eg Integer.MAX_VALUE) since that seems to cause the opposite effect of what we want (at least in Android 4.3)

		return result;
	}

	private long millisecondsFromYears(long years) {
		return years * 365L * 24L * 60L * 60L * 1000L;
	}

	private void hideTimestamp(NotificationBuilderCompat builder) {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}
}
