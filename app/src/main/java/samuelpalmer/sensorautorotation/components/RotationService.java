package samuelpalmer.sensorautorotation.components;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserActiveState;
import samuelpalmer.sensorautorotation.UserErrorHandler;
import samuelpalmer.sensorautorotation.UserInteractivity;
import samuelpalmer.sensorautorotation.UserInteractivityMonitor;

public class RotationService extends Service {

	private boolean destroyed;
	private UserActiveState userActiveState;

	public static ScreenOrientationController screenOrientationController;
	public static OrientationCalculator calculator;

	private UserInteractivityMonitor userInteractivityMonitor;

	@Override
	public void onCreate() {
		super.onCreate();

		screenOrientationController = new ScreenOrientationController(this);
		calculator = new OrientationCalculator(this);

		userActiveState = new UserActiveState(this);

		userInteractivityMonitor = new UserInteractivityMonitor(this, new StateMonitor.Listener<UserInteractivity>() {
			@Override
			public void stateChanged(UserInteractivity user) {
				try {
					if (user.active())
						userActiveState.ensureStarted();
					else
						userActiveState.ensureStopped();
				} catch (EnvironmentException e) {
					new UserErrorHandler(RotationService.this).handle(e);
				}
			}
		});

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		try {
			screenOrientationController.ensureStarted();

			promoteToForeground();

			userInteractivityMonitor.ensureStarted();
		} catch (EnvironmentException e) {
			new UserErrorHandler(this).handle(e);
		}
	}

	private void destroy() {
		if (!destroyed) {
			try {
				userActiveState.ensureStopped();
				screenOrientationController.ensureStopped();
				unregisterReceiver(shutdownReceiver);
				userInteractivityMonitor.ensureStopped();
			} catch (EnvironmentException e) {
				new UserErrorHandler(this).handle(e);
			}

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
		return null;
	}

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
				.setPriority(NotificationCompat.PRIORITY_MIN)
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

}
