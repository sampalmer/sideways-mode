package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

/**
 * I'm using a global instead of Android's binding mechanism.
 * This is because the service's process is mostly a single-threaded process and it
 * avoids the complexities of binding and inability to determine service running state.
 */
public class ApplicationInstance extends State {
	
	private static final String TAG = ApplicationInstance.class.getSimpleName();
	
	private OrientationCalculator calculator;
	ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private ServiceRevivalManager serviceRevivalManager;
	private OrientationSensorHandler orientationSensorHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementManager orientationEnforcementManager;
	ActivityMonitor activityMonitor;
	private ScreenOrientationUpdater screenUpdater;
	private UserInteractivityMonitor userInteractivityMonitor;
	
	public ApplicationInstance() {
		super();
	}
	
	@Override
	protected void start() {
		Log.i(TAG, "Application is starting");
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentErrorManager.clear();
		
		SystemSettings systemSettings = new SystemSettings();
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor();
		calculator = new OrientationCalculator(screenRotationMonitor);
		screenUpdater = new ScreenOrientationUpdater(calculator, systemSettings, screenRotationMonitor);
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(calculator);
		serviceRevivalManager = new ServiceRevivalManager();
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor);
		
		systemAutoRotationMonitor.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();
		
		userInteractivityMonitor = new UserInteractivityMonitor();
		userInteractivityMonitor.subscribe(userInteractivityListener);
		userInteractivityListener.update();
	}
	
	@Override
	protected void stop() {
		Log.i(TAG, "Application is stopping");
		
		userInteractivityMonitor.unsubscribe(userInteractivityListener);
		orientationSensorHandler.ensureStopped();
		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenUpdater.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();
		
		ServiceSettings.instance().persistentState().remove();
	}
	
	public void setSide(UserSide side) {
		calculator.setSide(side);
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
	
}
