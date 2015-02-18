package samuelpalmer.sensorautorotation;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.components.ToggleRotationLockReceiver;

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
		Intents intents = new Intents(service);

		Notification.Builder builder = new Notification.Builder(service)
				.setSmallIcon(mode().iconResourceId())
				.setContentIntent(intents.makePending(intents.makeIntent(ToggleRotationLockReceiver.class), Intents.ComponentType.BROADCAST_RECEIVER))
				.setContentTitle(service.getText(mode().summaryResourceId()))
				.setContentText(service.getText(mode().actionDescriptionResourceId()));

		moveToTop(builder);
		hideTimestamp(builder);
		hideFromLockScreen(builder);

		if (Build.VERSION.SDK_INT >= 21)
			builder.setCategory(Notification.CATEGORY_SERVICE);

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

		builder.setWhen(Integer.MAX_VALUE);
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
			int currentScreenOrientation = service.getResources().getConfiguration().orientation;
			boolean screenIsPortrait = currentScreenOrientation == Configuration.ORIENTATION_PORTRAIT;

			WindowManager windowManager = (WindowManager)service.getSystemService(Context.WINDOW_SERVICE);
			Orientation currentScreenRotation = new AndroidSurfaceRotation(windowManager.getDefaultDisplay().getRotation()).orientation();
			boolean screenIsNaturalOrientation = currentScreenRotation.isNaturalOrUpsideDown();

			boolean deviceIsNaturallyPortrait = screenIsPortrait == screenIsNaturalOrientation;

			boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
			return inNaturalOrientation == deviceIsNaturallyPortrait;
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
