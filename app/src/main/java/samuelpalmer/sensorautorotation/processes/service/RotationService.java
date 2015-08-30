package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.notifications.service.NotificationServiceStatusDisplay;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusGenerator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.upgrade.Upgrader;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityChangeHandler;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

/**
 * Encapsulates both the settings server (for the UI) and the main background rotation logic.
 * They're both using this service since otherwise Android sometimes kills the main service when
 * the main activity task is removed.
 *
 * http://stackoverflow.com/questions/30892299/removing-task-kills-background-process-when-bound
 */
public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {

	public static final String ACTION_TOGGLE = "toggle";
	public static final String ACTION_ROTATE_LEFT = "rotate_left";
	public static final String ACTION_ROTATE_RIGHT = "rotate_right";
	public static final String ACTION_STOP = "stop";

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceStatusGenerator serviceStatusGenerator;
	private UserInteractivityChangeHandler userInteractivityChangeHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		//Data version should be up-to-date before the service even tries to do anything
		new Upgrader().upgradeIfNecessary();

		//Expose this straight away in case it's needed to stop the service due to an initialisation failure
		ServiceHolder.started(this);

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen();

		applicationState = new ApplicationState(systemSettings);

		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);

		calculator = new OrientationCalculator(screenRotationMonitor, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor, calculator);

		serviceStatusGenerator = new ServiceStatusGenerator(calculator, screen, screenRotationMonitor);
		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		serviceNotificationManager = new ServiceNotificationManager(serviceStatusGenerator, new NotificationServiceStatusDisplay(this, screenUpdater, calculator, screenRotationMonitor));
		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityChangeHandler = new UserInteractivityChangeHandler(calculator, screenUpdater);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		screenOrientationController.ensureStarted();
		serviceNotificationManager.ensureStarted();
		userInteractivityChangeHandler.ensureStarted();
		serviceRevivalManager.ensureStarted();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (action != null)
				handle(action);
		}

		return Service.START_STICKY;
	}

	public void handle(String action) {
		switch (action) {
			case ACTION_TOGGLE:
				toggleRotation();
				break;
			case ACTION_ROTATE_LEFT:
				calculator.offsetUser(-1);
				break;
			case ACTION_ROTATE_RIGHT:
				calculator.offsetUser(1);
				break;
			case ACTION_STOP:
				new ServiceSynchroniser(ApplicationSettings.local()).disableService();
				break;
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

	@Override
	public void onDestroy() {
		super.onDestroy();

		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityChangeHandler.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();

		ServiceHolder.stopped();
	}

	private void toggleRotation() {
		calculator.setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		CharSequence message = serviceStatusGenerator.generate().getSummary();
		ToastReceiver.send(message);
	}

}
