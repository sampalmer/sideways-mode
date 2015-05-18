package samuelpalmer.sensorautorotation.components;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.EnvironmentErrorNotifier;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.ServiceApiBinder;
import samuelpalmer.sensorautorotation.ServiceRevivalManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotification;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementMonitor;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;
import samuelpalmer.sensorautorotation.userinteractivity.UserInteractivityThing;

public class RotationService extends Service {

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceNotification serviceNotification;
	private UserInteractivityThing userInteractivityThing;
	private OrientationEnforcementMonitor orientationEnforcementMonitor;
	private ActivityMonitor activityMonitor;

	@Override
	public void onCreate() {
		super.onCreate();

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		new EnvironmentErrorNotifier(this).clear();

		SystemSettings systemSettings = new SystemSettings(getContentResolver());
		Screen screen = new Screen(this);
		ApplicationSettings appSettings = new ApplicationSettings(this);

		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(appSettings, screen, applicationState);
		screenOrientationController = new ScreenOrientationController(this, systemSettings, screen, calculator);

		serviceNotification = new ServiceNotification(this, appSettings, calculator);
		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		serviceNotificationManager = new ServiceNotificationManager(this, serviceNotification, appSettings, screenUpdater);
		serviceRevivalManager = new ServiceRevivalManager(this);

		userInteractivityThing = new UserInteractivityThing(this, calculator, screenUpdater);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementMonitor = new OrientationEnforcementMonitor(this, appSettings, activityMonitor);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();
		serviceNotificationManager.ensureStarted();
		userInteractivityThing.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementMonitor.ensureStarted();
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
		return new ServiceApiBinder(this, serviceNotification, calculator, activityMonitor);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		orientationEnforcementMonitor.ensureStopped();
		activityMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityThing.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();
	}

}
