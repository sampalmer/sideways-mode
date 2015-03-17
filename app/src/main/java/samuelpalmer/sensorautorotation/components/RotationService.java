package samuelpalmer.sensorautorotation.components;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.OrientationMessenger;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
import samuelpalmer.sensorautorotation.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.ServiceApiBinder;
import samuelpalmer.sensorautorotation.ServiceRevivalManager;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserInteractivity;
import samuelpalmer.sensorautorotation.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class RotationService extends Service {

	private UserInteractivityMonitor userInteractivityMonitor;
	private ScreenOrientationController screenOrientationController;
	private ScreenOrientationUpdater screenUpdater;
	private OrientationMessenger orientationMessenger;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceNotification serviceNotification;

	@Override
	public void onCreate() {
		super.onCreate();

		SystemSettings systemSettings = new SystemSettings(getContentResolver());
		Screen screen = new Screen(this);

		ApplicationSettings appSettings = new ApplicationSettings(this);
		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(appSettings, screen, applicationState);
		screenOrientationController = new ScreenOrientationController(this, systemSettings, screen, calculator);

		screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		orientationMessenger = new OrientationMessenger(this, calculator);

		serviceNotification = new ServiceNotification(this, appSettings, calculator);
		serviceNotificationManager = new ServiceNotificationManager(this, serviceNotification, appSettings, calculator);
		serviceRevivalManager = new ServiceRevivalManager(this);

		userInteractivityMonitor = new UserInteractivityMonitor(this, new StateMonitor.Listener<UserInteractivity>() {
			@Override
			public void stateChanged(UserInteractivity user) {
				if (user.active()) {
					screenUpdater.ensureStarted();
					orientationMessenger.ensureStarted();
				}
				else {
					screenUpdater.ensureStopped();
					orientationMessenger.ensureStopped();

					calculator.reset();
				}
			}
		});

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();
		serviceNotificationManager.ensureStarted();
		userInteractivityMonitor.ensureStarted();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return new ServiceApiBinder(this, serviceNotification, calculator);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		orientationMessenger.ensureStopped();
		screenUpdater.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityMonitor.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();
	}

}
