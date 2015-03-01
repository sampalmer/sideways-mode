package samuelpalmer.sensorautorotation;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.components.ToggleRotationLockActivity;

public class ServiceNotification {

	private final Service service;
	private final OrientationCalculator calculator;

	public ServiceNotification(Service service, OrientationCalculator calculator) {
		this.service = service;
		this.calculator = calculator;
	}

	/**
	 * Reduces chance of being killed
	 */
	public void set() {
		Intents intents = new Intents(service, ToggleRotationLockActivity.class);

		//Intentionally not using NotificationCompat since setShowWhen doesn't work in it
		Notification.Builder builder = new Notification.Builder(service)
				.setSmallIcon(mode().iconResourceId())
				.setContentIntent(intents.makePending())
				.setContentTitle(service.getText(mode().summaryResourceId()))
				.setContentText(service.getText(mode().actionDescriptionResourceId()));

		moveToTop(builder);
		hideTimestamp(builder);
		hideFromLockScreen(builder);

		if (Build.VERSION.SDK_INT >= 21)
			builder.setCategory(Notification.CATEGORY_STATUS);

		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();

		service.startForeground(NotificationIds.SERVICE, notification);
	}

	private void hideFromLockScreen(Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= 21) {
			//This seems to be the best we've got: http://stackoverflow.com/questions/27035202/how-to-suppress-notification-on-lock-screen-in-android-5-lollipop-but-let-it-i
			builder.setVisibility(Notification.VISIBILITY_SECRET);

			KeyguardManager keyguardManager = (KeyguardManager) service.getSystemService(Context.KEYGUARD_SERVICE);

			if (keyguardManager.isKeyguardLocked())
				builder.setPriority(Notification.PRIORITY_MIN);
		}
	}

	private void moveToTop(Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= 16)
			builder.setPriority(Notification.PRIORITY_MAX);

		builder.setWhen(bestWhen());
	}

	private long bestWhen() {
		long result = System.currentTimeMillis();

		if (Build.VERSION.SDK_INT < 21) //Because Android 5 ignores future 'when' values in ordering
			result += millisecondsFromYears(10L);

		return result;
	}

	private long millisecondsFromYears(long years) {
		return years * 365L * 24L * 60L * 60L * 1000L;
	}

	private void hideTimestamp(Notification.Builder builder) {
		if (Build.VERSION.SDK_INT >= 17)
			builder.setShowWhen(false);
		else
			builder.setWhen(0);
	}

	public Mode mode() {
		if (calculator.isRecalibrating())
			return new LockedMode();
		else
			return new AutoRotationMode();
	}

	public interface Mode {
		public int iconResourceId();
		public int summaryResourceId();
		public int actionDescriptionResourceId();
	}

	private class LockedMode implements Mode {

		@Override
		public int iconResourceId() {
			return isPortrait() ? R.drawable.ic_screen_rotation_locked_portrait : R.drawable.ic_screen_rotation_locked_landscape;
		}

		public boolean isPortrait() {
			boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
			return inNaturalOrientation == deviceIsNaturallyPortrait();
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		private boolean deviceIsNaturallyPortrait() {
			WindowManager windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
			Display display = windowManager.getDefaultDisplay();
			Point screenResolution = new Point();

			//Getting the screen dimensions and rotation should be as close together as possible
			//since it's possible for them to be retrieved out of sync
			if (Build.VERSION.SDK_INT >= 14)
				display.getRealSize(screenResolution);
			else
				display.getSize(screenResolution);
			int rotation = display.getRotation();

			Orientation orientation = new AndroidSurfaceRotation(rotation).orientation();

			boolean portrait = screenResolution.x < screenResolution.y;
			return portrait == orientation.isNaturalOrUpsideDown();
		}

		@Override
		public int summaryResourceId() {
			return R.string.screen_rotation_disabled;
		}

		@Override
		public int actionDescriptionResourceId() {
			return R.string.enable_screen_rotation;
		}
	}

	private class AutoRotationMode implements Mode {

		@Override
		public int iconResourceId() {
			return R.drawable.ic_screen_rotation_enabled;
		}

		@Override
		public int summaryResourceId() {
			return R.string.screen_rotation_enabled;
		}

		@Override
		public int actionDescriptionResourceId() {
			return R.string.disable_screen_rotation;
		}
	}
}
