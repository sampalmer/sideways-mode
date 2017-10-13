package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusDisplayUpdater;
import samuelpalmer.sensorautorotation.notifications.service.ApplicationStatusFactory;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

import static android.content.Context.WINDOW_SERVICE;

/**
 * I'm using a global instead of Android's binding mechanism.
 * This is because the service's process is mostly a single-threaded process and it
 * avoids the complexities of binding and inability to determine service running state.
 */
public class ApplicationInstance extends State {
	
	private static final String TAG = ApplicationInstance.class.getSimpleName();
	
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
		Display screen = ((WindowManager) CurrentProcess.context().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
		applicationState = new ApplicationState();
		
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(screen);
		
		calculator = new OrientationCalculator(screenRotationMonitor, applicationState);
		screenOrientationController = new ScreenOrientationController(systemSettings, screenRotationMonitor);
		
		screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		LockScreenMonitor lockScreenMonitor = new LockScreenMonitor(CurrentProcess.context());
		applicationStatusFactory = new ApplicationStatusFactory(calculator);
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(applicationStatusFactory);
		
		serviceRevivalManager = new ServiceRevivalManager();
		
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings, calculator);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor, lockScreenMonitor);
		
		systemAutoRotationMonitor.ensureStarted();
		screenOrientationController.ensureStarted();
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
		Log.i(TAG, "Application is starting");
		
		userInteractivityMonitor.unsubscribe(userInteractivityListener);
		orientationSensorHandler.ensureStopped();
		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenOrientationController.ensureStopped();
		screenUpdater.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		serviceRevivalManager.ensureStopped();
		
		applicationState.clear();
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
