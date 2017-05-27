package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.NaturalOrientationDetector;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.Screen;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplays;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusFactory;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.api.RotationServiceCommand;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityChangeHandler;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {

	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	ServiceStatusDisplayUpdater serviceStatusDisplayUpdater;
	private ServiceRevivalManager serviceRevivalManager;
	private ServiceStatusFactory serviceStatusFactory;
	private UserInteractivityChangeHandler userInteractivityChangeHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementManager orientationEnforcementManager;
	ActivityMonitor activityMonitor;
	private ServiceStatusDisplays display;

	@Override
	public void onCreate() {
		super.onCreate();

		//TODO: Maybe check if the service is actually enabled and stop if it's not?

		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();

		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen(SystemServices.windowManager());

		applicationState = new ApplicationState(systemSettings);

		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);

		calculator = new OrientationCalculator(screenRotationMonitor, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor, applicationState);

		ScreenOrientationUpdater screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		LockScreenMonitor lockScreenMonitor = new LockScreenMonitor(CurrentProcess.context());
		serviceStatusFactory = new ServiceStatusFactory(calculator, screenRotationMonitor, new NaturalOrientationDetector(screen), screenUpdater, lockScreenMonitor);
		display = new ServiceStatusDisplays(this);
		serviceStatusDisplayUpdater = new ServiceStatusDisplayUpdater(serviceStatusFactory, display);

		serviceRevivalManager = new ServiceRevivalManager();

		userInteractivityChangeHandler = new UserInteractivityChangeHandler(calculator, screenUpdater, screenRotationMonitor);

		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));

		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor, lockScreenMonitor);

		systemAutoRotationMonitor.ensureStarted();
		screenOrientationController.ensureStarted();
		serviceStatusDisplayUpdater.ensureStarted();
		userInteractivityChangeHandler.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();

		//Don't want anything accessing the service 'til it's finished initialising.
		ServiceHolder.started(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null)
			handle(RotationServiceCommand.fromIntent(intent));

		return Service.START_STICKY;
	}

	public void handle(RotationServiceCommand command) {
		switch (command.action) {
			case RotationServiceCommand.ACTION_TOGGLE:
				calculator.setAutoRotate(!calculator.autoRotate());
				break;
			case RotationServiceCommand.ACTION_ROTATE_LEFT:
				calculator.offsetScreenBy(new Rotation(-1));
				break;
			case RotationServiceCommand.ACTION_ROTATE_RIGHT:
				calculator.offsetScreenBy(new Rotation(1));
				break;
			case RotationServiceCommand.ACTION_UPDATE_NOTIFICATION:
				display.update(serviceStatusDisplayUpdater.getLastStatus());
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}

		if (command.showToast) {
			CharSequence status = serviceStatusFactory.generate().getSummary();
			ToastReceiver.send(status, Toast.LENGTH_SHORT);
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

		//Don't want anything accessing the service during deinitialisation.
		ServiceHolder.stopped();

		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		unregisterReceiver(shutdownReceiver);
		userInteractivityChangeHandler.ensureStopped();
		serviceStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();

		applicationState.clear();
	}
	
}
