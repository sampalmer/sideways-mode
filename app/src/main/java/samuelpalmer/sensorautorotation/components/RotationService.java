package samuelpalmer.sensorautorotation.components;


import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.ActivityMonitor;
import samuelpalmer.sensorautorotation.AppPackage;
import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.OrientationEnforcementMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserActiveState;
import samuelpalmer.sensorautorotation.UserInteractivity;
import samuelpalmer.sensorautorotation.UserInteractivityMonitor;

public class RotationService extends AccessibilityService {

	private boolean destroyed;
	private UserActiveState userActiveState;

	public static ScreenOrientationController screenOrientationController;
	public static OrientationCalculator calculator;

	private UserInteractivityMonitor userInteractivityMonitor;
	private OrientationEnforcementMonitor orientationEnforcementMonitor;
	private ActivityMonitor activityMonitor;

	@Override
	public void onCreate() {
		super.onCreate();

		screenOrientationController = new ScreenOrientationController(this);
		calculator = new OrientationCalculator(this);

		userActiveState = new UserActiveState(this);

		userInteractivityMonitor = new UserInteractivityMonitor(this, new StateMonitor.Listener<UserInteractivity>() {
			@Override
			public void stateChanged(UserInteractivity user) {
				if (user.active())
					userActiveState.ensureStarted();
				else
					userActiveState.ensureStopped();
			}
		});

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		activityMonitor = new ActivityMonitor();
		orientationEnforcementMonitor = new OrientationEnforcementMonitor(this, activityMonitor);

		screenOrientationController.ensureStarted();

		promoteToForeground();

		userInteractivityMonitor.ensureStarted();
		orientationEnforcementMonitor.ensureStarted();
	}

	private void destroy() {
		if (!destroyed) {
			userActiveState.ensureStopped();
			screenOrientationController.ensureStopped();
			unregisterReceiver(shutdownReceiver);
			userInteractivityMonitor.ensureStopped();
			orientationEnforcementMonitor.ensureStopped();

			destroyed = true;
		}
	}

	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			stopSelf();
		}
	};

	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Intents intents = new Intents(this);

		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(intents.makePending(RecalibrationActivity.class))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Touch to change user orientation")
				.build();

		startForeground(1, notification);
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

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {

		boolean windowChanged = event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		if (windowChanged && isForActivity(event))
			activityMonitor.reportChange(new AppPackage(event.getPackageName()));

	}

	private boolean isForActivity(AccessibilityEvent event) {
		ComponentName activityComponentName = new ComponentName(
				event.getPackageName().toString(),
				event.getClassName().toString());

		try {
			getPackageManager().getActivityInfo(activityComponentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}

		return true;
	}

	@Override
	public void onInterrupt() {}
}
