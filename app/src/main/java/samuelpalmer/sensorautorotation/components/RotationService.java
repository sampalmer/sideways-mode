package samuelpalmer.sensorautorotation.components;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.OrientationMessenger;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
import samuelpalmer.sensorautorotation.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserInteractivity;
import samuelpalmer.sensorautorotation.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.android.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class RotationService extends Service {

	private boolean destroyed;

	public static ScreenOrientationController screenOrientationController;
	public static OrientationCalculator calculator;

	private UserInteractivityMonitor userInteractivityMonitor;
	private ScreenOrientationUpdater screenUpdater;
	private OrientationMessenger orientationMessenger;

	@Override
	public void onCreate() {
		super.onCreate();

		SystemSettings settings = new SystemSettings(new AndroidSystemSettingSource(getContentResolver()));
		Screen screen = new Screen(this);

		calculator = new OrientationCalculator(screen, settings);
		screenOrientationController = new ScreenOrientationController(this, settings, screen, calculator);

		screenUpdater = new ScreenOrientationUpdater(this, RotationService.calculator, RotationService.screenOrientationController);
		orientationMessenger = new OrientationMessenger(this);

		userInteractivityMonitor = new UserInteractivityMonitor(this, new StateMonitor.Listener<UserInteractivity>() {
			@Override
			public void stateChanged(UserInteractivity user) {
				if (user.active()) {
					screenUpdater.ensureStarted();
					orientationMessenger.ensureStarted();
				}
				else {
					orientationMessenger.ensureStopped();

					calculator.reset();
					promoteToForeground();

					screenUpdater.ensureStopped();
				}
			}
		});

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();

		promoteToForeground();

		userInteractivityMonitor.ensureStarted();
	}

	private void destroy() {
		if (!destroyed) {
			orientationMessenger.ensureStopped();
			screenUpdater.ensureStopped();
			screenOrientationController.ensureStopped();
			unregisterReceiver(shutdownReceiver);
			userInteractivityMonitor.ensureStopped();

			destroyed = true;
		}
	}

	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}

	/**
	 * Must be called from process main thread
	 */
	public class LocalBinder extends Binder {

		public void toggleRotation() {
			setRecalibrating(!calculator.isRecalibrating());
		}

	}

	public void setRecalibrating(boolean newValue) {
		calculator.setRecalibrating(newValue);
		promoteToForeground();
	}

	/**
	 * Reduces chance of being killed
	 */
	public void promoteToForeground() {
		Intents intents = new Intents(this);

		Notification.Builder builder = new Notification.Builder(this)
				.setSmallIcon(mode().iconResourceId())
				.setContentIntent(intents.makePending(ToggleRotationLockActivity.class))
				.setContentTitle(getText(mode().summaryResourceId()))
				.setContentText(getText(mode().actionDescriptionResourceId()));

		if (Build.VERSION.SDK_INT >= 16)
			builder.setPriority(NotificationCompat.PRIORITY_MAX);

		if (Build.VERSION.SDK_INT >= 17) {
			builder
					.setShowWhen(false)
					.setWhen(Integer.MAX_VALUE);
		}
		else {
			builder.setWhen(0);
		}

		if (Build.VERSION.SDK_INT >= 21)
			builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();

		startForeground(1, notification);
	}

	private Mode mode() {
		if (calculator.isRecalibrating())
			return new LockedMode();
		else
			return new AutoRotationMode();
	}

	private interface Mode {
		public int iconResourceId();
		public int summaryResourceId();
		public int actionDescriptionResourceId();
	}

	private class LockedMode implements Mode {

		@Override
		public int iconResourceId() {
			return calculator.proposedScreenOrientation().isPortrait(RotationService.this) ? R.drawable.ic_screen_rotation_locked_portrait : R.drawable.ic_screen_rotation_locked_landscape;
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

	@Override
	public void onDestroy() {
		super.onDestroy();

		destroy();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		destroy();
	}

}
