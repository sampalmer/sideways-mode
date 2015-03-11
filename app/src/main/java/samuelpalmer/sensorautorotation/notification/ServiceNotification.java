package samuelpalmer.sensorautorotation.notification;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.os.Build;

import samuelpalmer.sensorautorotation.ApplicationSettings;
import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.components.ToggleRotationLockActivity;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public class ServiceNotification extends State {

	private final Service service;
	private final OrientationCalculator calculator;
	private final ApplicationSettings settings;
	private final EventRegistrar<Void> onShowNotificationAtTopChanged;

	public ServiceNotification(Service service, OrientationCalculator calculator, ApplicationSettings settings) {
		this.service = service;
		this.calculator = calculator;
		this.settings = settings;
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
	}

	/**
	 * Reduces chance of being killed
	 */
	public void set() {
		Intents intents = new Intents(service, ToggleRotationLockActivity.class);

		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		NotificationBuilderCompat builder = NotificationBuilderCompat.create(service)
				.setSmallIcon(mode().iconResourceId())
				.setContentIntent(intents.makePending())
				.setContentTitle(service.getText(mode().summaryResourceId()))
				.setContentText(service.getText(mode().actionDescriptionResourceId()));

		if (settings.showNotificationAtTop().get())
			moveToTop(builder);

		hideTimestamp(builder);
		hideFromLockScreen(builder);

		builder.setCategory(NotificationBuilderCompat.CATEGORY_STATUS);
		builder.setColor(0xFF795548);

		Notification notification = builder.build();

		service.startForeground(NotificationIds.SERVICE, notification);
	}

	private void hideFromLockScreen(NotificationBuilderCompat builder) {
		if (Build.VERSION.SDK_INT >= 21) {
			//This seems to be the best we've got: http://stackoverflow.com/questions/27035202/how-to-suppress-notification-on-lock-screen-in-android-5-lollipop-but-let-it-i
			builder.setVisibility(NotificationBuilderCompat.VISIBILITY_SECRET);

			KeyguardManager keyguardManager = (KeyguardManager) service.getSystemService(Context.KEYGUARD_SERVICE);

			if (keyguardManager.isKeyguardLocked())
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

	public Mode mode() {
		if (calculator.isRecalibrating())
			return new LockedMode(service, calculator);
		else
			return new AutoRotationMode();
	}

	@Override
	protected void start() {
		set();
		onShowNotificationAtTopChanged.subscribe(updateNotification);
	}

	@Override
	protected void stop() {
		onShowNotificationAtTopChanged.unsubscribe(updateNotification);
	}

	private final EventHandler<Void> updateNotification = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			set();
		}
	};

}
