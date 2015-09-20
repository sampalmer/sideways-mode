package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.NaturalOrientationDetector;
import samuelpalmer.sensorautorotation.notifications.service.NotificationParametersFactory;
import samuelpalmer.sensorautorotation.notifications.service.NotificationUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityChangeHandler;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.Setting;
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

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	private ServiceNotificationManager serviceNotificationManager;
	private ServiceRevivalManager serviceRevivalManager;
	private NotificationParametersFactory notificationParametersFactory;
	private UserInteractivityChangeHandler userInteractivityChangeHandler;
	private LockScreenUserOrientationInferenceManager userOrientationInferrer;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;

	@Override
	public void onCreate() {
		super.onCreate();

		//Expose this straight away in case it's needed to stop the service due to an initialisation failure
		ServiceHolder.started(this);

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen();

		applicationState = new ApplicationState(systemSettings);

		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);

		Setting<Orientation> proposedScreenOrientation = new CachedSetting<>(systemSettings.userRotation);
		calculator = new OrientationCalculator(screenRotationMonitor, applicationState, proposedScreenOrientation);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor, calculator, proposedScreenOrientation);

		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		LockScreenMonitor lockScreenMonitor = new LockScreenMonitor();
		notificationParametersFactory = new NotificationParametersFactory(calculator, screenRotationMonitor, new NaturalOrientationDetector(screen), screenUpdater, lockScreenMonitor);
		serviceNotificationManager = new ServiceNotificationManager(notificationParametersFactory, new NotificationUpdater(this));
		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityChangeHandler = new UserInteractivityChangeHandler(calculator, screenUpdater, screenRotationMonitor);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		userOrientationInferrer = new LockScreenUserOrientationInferenceManager(new LockScreenUserOrientationInferrer(lockScreenMonitor, calculator));

		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);

		systemAutoRotationMonitor.ensureStarted();

		//CORE STARTUP LOGIC: ORDER IS CRITICAL
		screenOrientationController.ensureStarted();
		calculator.reset();

		serviceNotificationManager.ensureStarted();
		userInteractivityChangeHandler.ensureStarted();
		serviceRevivalManager.ensureStarted();
		userOrientationInferrer.ensureStarted();
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

		systemAutoRotationMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityChangeHandler.ensureStopped();
		serviceNotificationManager.ensureStopped();
		serviceRevivalManager.ensureStopped();
		userOrientationInferrer.ensureStopped();

		applicationState.clear();

		ServiceHolder.stopped();
	}

	private void toggleRotation() {
		calculator.setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		CharSequence message = notificationParametersFactory.generate().getSummary();
		ToastReceiver.send(message);
	}

}
