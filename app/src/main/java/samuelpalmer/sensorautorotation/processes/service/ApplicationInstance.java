package samuelpalmer.sensorautorotation.processes.service;

import android.widget.Toast;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.NaturalOrientationDetector;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.Screen;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplays;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusFactory;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityChangeHandler;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

/**
 * I'm using a global instead of Android's binding mechanism.
 * This is because the service's process is mostly a single-threaded process and it
 * avoids the complexities of binding and inability to determine service running state.
 */
public class ApplicationInstance extends State {
	
	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private ServiceRevivalManager serviceRevivalManager;
	private ApplicationStatusFactory applicationStatusFactory;
	private UserInteractivityChangeHandler userInteractivityChangeHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementManager orientationEnforcementManager;
	ActivityMonitor activityMonitor;
	private ApplicationStatusDisplays display;
	
	@Override
	protected void start() {
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
		applicationStatusFactory = new ApplicationStatusFactory(calculator, screenRotationMonitor, new NaturalOrientationDetector(screen), screenUpdater, lockScreenMonitor);
		display = new ApplicationStatusDisplays();
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(applicationStatusFactory, display);
		
		serviceRevivalManager = new ServiceRevivalManager();
		
		userInteractivityChangeHandler = new UserInteractivityChangeHandler(calculator, screenUpdater, screenRotationMonitor);
		
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor, lockScreenMonitor);
		
		systemAutoRotationMonitor.ensureStarted();
		screenOrientationController.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		userInteractivityChangeHandler.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();
	}
	
	@Override
	protected void stop() {
		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		userInteractivityChangeHandler.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();
		
		applicationState.clear();
	}
	
	public void handle(ApplicationInstanceCommand command) {
		switch (command.action) {
			case ApplicationInstanceCommand.ACTION_TOGGLE:
				calculator.setAutoRotate(!calculator.autoRotate());
				break;
			case ApplicationInstanceCommand.ACTION_ROTATE_LEFT:
				calculator.offsetScreenBy(new Rotation(-1));
				break;
			case ApplicationInstanceCommand.ACTION_ROTATE_RIGHT:
				calculator.offsetScreenBy(new Rotation(1));
				break;
			case ApplicationInstanceCommand.ACTION_UPDATE_NOTIFICATION:
				display.update(applicationStatusDisplayUpdater.getLastStatus());
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}
		
		if (command.showToast) {
			CharSequence status = applicationStatusFactory.generate().getSummary();
			ToastReceiver.send(status, Toast.LENGTH_SHORT);
		}
	}
	
}
