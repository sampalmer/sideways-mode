package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.Screen;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplays;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusFactory;
import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

/**
 * I'm using a global instead of Android's binding mechanism.
 * This is because the service's process is mostly a single-threaded process and it
 * avoids the complexities of binding and inability to determine service running state.
 */
public class ApplicationInstance extends State {
	
	private final UserSide initialUserSide;
	
	private ScreenOrientationController screenOrientationController;
	private ApplicationState applicationState;
	private OrientationCalculator calculator;
	ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private ServiceRevivalManager serviceRevivalManager;
	private ApplicationStatusFactory applicationStatusFactory;
	private OrientationSensorHandler orientationSensorHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementManager orientationEnforcementManager;
	ActivityMonitor activityMonitor;
	private ApplicationStatusDisplays display;
	private ScreenOrientationUpdater screenUpdater;
	
	public ApplicationInstance(UserSide initialUserSide) {
		super();
		this.initialUserSide = initialUserSide;
	}
	
	@Override
	protected void start() {
		//TODO: Maybe check if the service is actually enabled and stop if it's not?
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentError.clear();
		
		SystemSettings systemSettings = new SystemSettings();
		Screen screen = new Screen(SystemServices.windowManager());
		
		applicationState = new ApplicationState(initialUserSide);
		
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);
		
		calculator = new OrientationCalculator(screenRotationMonitor, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor, applicationState);
		
		screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		LockScreenMonitor lockScreenMonitor = new LockScreenMonitor(CurrentProcess.context());
		applicationStatusFactory = new ApplicationStatusFactory(calculator, screenRotationMonitor, screenUpdater, lockScreenMonitor);
		display = new ApplicationStatusDisplays();
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(applicationStatusFactory, display);
		
		serviceRevivalManager = new ServiceRevivalManager();
		
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor, lockScreenMonitor);
		
		systemAutoRotationMonitor.ensureStarted();
		screenOrientationController.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		orientationSensorHandler.ensureStarted();
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
		orientationSensorHandler.ensureStopped();
		screenUpdater.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();
		
		applicationState.clear();
	}
	
	public void handle(ApplicationInstanceCommand command) {
		switch (command.action) {
			case ApplicationInstanceCommand.ACTION_CHANGE_SIDE:
				calculator.changeSide();
				break;
			case ApplicationInstanceCommand.ACTION_UPDATE_NOTIFICATION:
				display.update(applicationStatusDisplayUpdater.getLastStatus());
				break;
			case ApplicationInstanceCommand.ACTION_TURN_OFF:
				ApplicationSettings.instance().enabled().set(false);
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}
	}
	
}
