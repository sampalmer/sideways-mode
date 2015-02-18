package samuelpalmer.sensorautorotation.components;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.ApplicationSettings;
import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.OrientationMessenger;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
import samuelpalmer.sensorautorotation.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.ServiceNotification;
import samuelpalmer.sensorautorotation.ServiceNotificationLockScreenHider;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserInteractivity;
import samuelpalmer.sensorautorotation.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.android.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class RotationService extends Service {

	public static OrientationCalculator calculator;

	private UserInteractivityMonitor userInteractivityMonitor;
	private ScreenOrientationController screenOrientationController;
	private ScreenOrientationUpdater screenUpdater;
	private OrientationMessenger orientationMessenger;
	private ApplicationState applicationState;
	private ServiceNotification serviceNotification;
	private ServiceNotificationLockScreenHider notificationHider;

	@Override
	public void onCreate() {
		super.onCreate();

		SystemSettings systemSettings = new SystemSettings(new AndroidSystemSettingSource(getContentResolver()));
		Screen screen = new Screen(this);

		ApplicationSettings appSettings = new ApplicationSettings(this);
		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(appSettings, screen, applicationState);
		screenOrientationController = new ScreenOrientationController(this, systemSettings, screen, calculator);

		screenUpdater = new ScreenOrientationUpdater(appSettings, RotationService.calculator, screenOrientationController);
		orientationMessenger = new OrientationMessenger(this);

		serviceNotification = new ServiceNotification(this, calculator);
		notificationHider = new ServiceNotificationLockScreenHider(this, serviceNotification);

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
					serviceNotification.set();

					screenUpdater.ensureStopped();
				}
			}
		});

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();

		serviceNotification.set();

		userInteractivityMonitor.ensureStarted();
		notificationHider.ensureStarted();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (isToggleAction(intent)) {
			toggleRotation();
			if (intent.getBooleanExtra(EXTRA_SHOW_STATUS_TOAST, false))
				Toast
						.makeText(this, serviceNotification.mode().summaryResourceId(), Toast.LENGTH_SHORT)
						.show();
		}

		return Service.START_STICKY;
	}

	private boolean isToggleAction(Intent intent) {
		if (intent != null) {
			String action = intent.getAction();

			if (action != null && action.equals(getToggleAction(this)))
				return true;
		}

		return false;
	}

	public static String getToggleAction(Context service) {
		return service.getPackageName() + ".TOGGLE_ROTATION";
	}

	public static final String EXTRA_SHOW_STATUS_TOAST = "show_status_toast";

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

	public void toggleRotation() {
		setRecalibrating(!calculator.isRecalibrating());
	}

	public void setRecalibrating(boolean newValue) {
		calculator.setRecalibrating(newValue);
		serviceNotification.set();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		notificationHider.ensureStopped();
		orientationMessenger.ensureStopped();
		screenUpdater.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityMonitor.ensureStopped();

		applicationState.clear();
	}

}
