package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ApplicationState;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.CompositeServiceStatusDisplay;
import samuelpalmer.sensorautorotation.notifications.service.ServiceNotificationManager;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusGenerator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
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

		//Expose this straight away in case it's needed to stop the service due to an initialisation failure
		ServiceHolder.started(this);

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen();
		ApplicationSettings appSettings = new ApplicationSettings();

		applicationState = new ApplicationState(appSettings, systemSettings);

		calculator = new OrientationCalculator(screen, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screen, calculator);

		serviceStatusGenerator = new ServiceStatusGenerator(calculator, screen);
		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		serviceNotificationManager = new ServiceNotificationManager(serviceStatusGenerator, new CompositeServiceStatusDisplay(this, appSettings), screenUpdater);
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
		if (intent != null && ACTION_TOGGLE.equals(intent.getAction()))
			toggleRotation();

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

	public void toggleRotation() {
		calculator.setAutoRotate(!calculator.autoRotate());
		showStatusToast();
	}

	private void showStatusToast() {
		CharSequence message = serviceStatusGenerator.getSummary(serviceStatusGenerator.mode());
		ToastReceiver.send(message);
	}

}
