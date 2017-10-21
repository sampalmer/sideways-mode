package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceRunningState extends State {
	
	private static final String TAG = ServiceRunningState.class.getSimpleName();
	
	private final OrientationCalculator calculator;
	final ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private final ServiceRevivalManager serviceRevivalManager;
	private final OrientationSensorHandler orientationSensorHandler;
	private final SystemAutoRotationMonitor systemAutoRotationMonitor;
	private final OrientationEnforcementManager orientationEnforcementManager;
	final ActivityMonitor activityMonitor;
	private final ScreenOrientationUpdater screenUpdater;
	private final AppDisabler appDisabler;
	private final UserInteractivityMonitor userInteractivityMonitor;
	
	public ServiceRunningState(RotationService service) {
		SystemSettings systemSettings = new SystemSettings();
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor();
		calculator = new OrientationCalculator(screenRotationMonitor);
		screenUpdater = new ScreenOrientationUpdater(calculator, systemSettings, screenRotationMonitor);
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(calculator, service);
		serviceRevivalManager = new ServiceRevivalManager();
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor);
		appDisabler = new AppDisabler();
		userInteractivityMonitor = new UserInteractivityMonitor();
	}
	
	@Override
	protected void start() {
		Log.i(TAG, "App is turning on");
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentErrorManager.clear();
		
		systemAutoRotationMonitor.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();
		appDisabler.ensureStarted();
		
		userInteractivityMonitor.subscribe(userInteractivityListener);
		userInteractivityListener.update();
		
		ServiceRunningStateHolder.serviceStarted(this);
	}
	
	@Override
	protected void stop() {
		Log.i(TAG, "App is turning off");
		
		ServiceRunningStateHolder.serviceStopped();
		
		userInteractivityMonitor.unsubscribe(userInteractivityListener);
		appDisabler.ensureStopped();
		orientationSensorHandler.ensureStopped();
		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenUpdater.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();
		
		ServiceSettings.instance().persistentState().remove();
	}
	
	private final EventHandler userInteractivityListener = new EventHandler() {
		@Override
		public void update() {
			// We should only monitor the sensors while the screen is on. Otherwise it will waste battery.
			if (userInteractivityMonitor.get().active()) {
				Log.i(TAG, "User is interactive. Listening to sensors.");
				orientationSensorHandler.ensureStarted();
			}
			else {
				Log.i(TAG, "User is not interactive. Not listening to sensors.");
				orientationSensorHandler.ensureStopped();
			}
		}
	};
	
	public void setSide(UserSide side) {
		calculator.setSide(side);
	}
	
}
