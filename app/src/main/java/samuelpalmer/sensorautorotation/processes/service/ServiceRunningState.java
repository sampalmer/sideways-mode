package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceRunningState extends State {
	
	private static final String TAG = ServiceRunningState.class.getSimpleName();
	
	private final OrientationCalculator calculator;
	final ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private final LowMemoryDetector lowMemoryDetector;
	private final OrientationSensorHandler orientationSensorHandler;
	private final SystemAutoRotationMonitor systemAutoRotationMonitor;
	private final OrientationEnforcementManager orientationEnforcementManager;
	final ActivityMonitor activityMonitor;
	private final ScreenOrientationUpdater screenUpdater;
	private final AppDisabler appDisabler;
	private final DeviceInteractivityMonitor deviceInteractivityMonitor;
	
	public ServiceRunningState(RotationService service) {
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor();
		calculator = new OrientationCalculator(screenRotationMonitor);
		screenUpdater = new ScreenOrientationUpdater(calculator, screenRotationMonitor);
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(calculator, service);
		lowMemoryDetector = new LowMemoryDetector();
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		systemAutoRotationMonitor = new SystemAutoRotationMonitor();
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor);
		appDisabler = new AppDisabler();
		deviceInteractivityMonitor = new DeviceInteractivityMonitor();
	}
	
	@Override
	protected void start() {
		Log.i(TAG, "App is turning on");
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentErrorManager.clear();
		
		systemAutoRotationMonitor.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		lowMemoryDetector.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();
		appDisabler.ensureStarted();
		
		deviceInteractivityMonitor.subscribe(userInteractivityListener);
		userInteractivityListener.update();
		
		ServiceRunningStateHolder.serviceStarted(this);
	}
	
	@Override
	protected void stop() {
		Log.i(TAG, "App is turning off");
		
		ServiceRunningStateHolder.serviceStopped();
		
		deviceInteractivityMonitor.unsubscribe(userInteractivityListener);
		appDisabler.ensureStopped();
		orientationSensorHandler.ensureStopped();
		orientationEnforcementManager.ensureStopped();
		activityMonitor.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenUpdater.ensureStopped();
		applicationStatusDisplayUpdater.ensureStopped();
		lowMemoryDetector.ensureStopped();
		
		ServiceSettings
			.getSharedPreferences(CurrentProcess.context())
			.edit()
			.remove(ServiceSettings.KEY_PERSISTENT_STATE)
			.apply();
	}
	
	private final EventHandler userInteractivityListener = new EventHandler() {
		@Override
		public void update() {
			// We should only monitor the sensors while the screen is on. Otherwise it will waste battery.
			if (deviceInteractivityMonitor.isInteractive()) {
				Log.i(TAG, "Device is interactive. Listening to sensors.");
				orientationSensorHandler.ensureStarted();
			}
			else {
				Log.i(TAG, "Device is not interactive. Not listening to sensors.");
				orientationSensorHandler.ensureStopped();
			}
		}
	};
	
	public void setSide(UserSide side) {
		calculator.setSide(side);
	}
	
}
