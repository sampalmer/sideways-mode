package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.IncompatibleAppsManager;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.DeviceInteractivityMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ServiceRunningState extends State {
	
	private static final String TAG = ServiceRunningState.class.getSimpleName();
	
	private final RotationService service;
	private final OrientationCalculator calculator;
	final RunningStateUpdater runningStateUpdater;
	private final LowMemoryDetector lowMemoryDetector;
	private final OrientationSensorHandler orientationSensorHandler;
	private final SystemAutoRotationMonitor systemAutoRotationMonitor;
	private final IncompatibleAppsManager incompatibleAppsManager;
	private final ScreenOrientationUpdater screenUpdater;
	private final AppDisabler appDisabler;
	private final DeviceInteractivityMonitor deviceInteractivityMonitor;

	public ServiceRunningState(RotationService service) {
		this.service = service;
		
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor(service);
		calculator = new OrientationCalculator(service, screenRotationMonitor);
		screenUpdater = new ScreenOrientationUpdater(service, calculator, screenRotationMonitor);
		runningStateUpdater = new RunningStateUpdater(service, calculator, service);
		lowMemoryDetector = new LowMemoryDetector(service);
		orientationSensorHandler = new OrientationSensorHandler(service, calculator, screenRotationMonitor);
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(service);
		ActivityMonitor activityMonitor = ActivityMonitor.create(service);
		incompatibleAppsManager = new IncompatibleAppsManager(service, activityMonitor);
		appDisabler = new AppDisabler(service);
		deviceInteractivityMonitor = new DeviceInteractivityMonitor(service);
	}
	
	@Override
	protected void start() {
		Log.i(TAG, "App is turning on");
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentErrorManager.clear(service);
		
		systemAutoRotationMonitor.ensureStarted();
		runningStateUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		lowMemoryDetector.ensureStarted();
		incompatibleAppsManager.ensureStarted();
		appDisabler.ensureStarted();

		deviceInteractivityMonitor.subscribe(deviceInteractivityListener);
		deviceInteractivityListener.update();
		
		ServiceRunningStateHolder.serviceStarted(this);
	}
	
	@Override
	protected void stop() {
		Log.i(TAG, "App is turning off");
		
		ServiceRunningStateHolder.serviceStopped();
		
		deviceInteractivityMonitor.unsubscribe(deviceInteractivityListener);
		appDisabler.ensureStopped();
		orientationSensorHandler.ensureStopped();
		incompatibleAppsManager.ensureStopped();
		systemAutoRotationMonitor.ensureStopped();
		screenUpdater.ensureStopped();
		runningStateUpdater.ensureStopped();
		lowMemoryDetector.ensureStopped();
		
		ServiceSettings
			.getSharedPreferences(service)
			.edit()
			.remove(ServiceSettings.KEY_PERSISTENT_STATE)
			.apply();
	}
	
	private final EventHandler deviceInteractivityListener = new EventHandler() {
		@Override
		public void update() {
			if (deviceInteractivityMonitor.isInteractive() != orientationSensorHandler.isStarted()) {
				// We should only monitor the sensors while the screen is on. Otherwise it will waste battery.
				if (deviceInteractivityMonitor.isInteractive()) {
					Log.i(TAG, "Device is interactive. Listening to sensors.");
					orientationSensorHandler.ensureStarted();
				} else {
					/*
					Don't want to turn the app off when the screen turns off because the screen might
					have turned off due to inactivity while the user is actually still using the device.
					 */
					Log.i(TAG, "Device is not interactive. Not listening to sensors.");
					orientationSensorHandler.ensureStopped();
				}
			}
		}
	};
	
	public void setSide(UserSide side) {
		calculator.setSide(side);
	}
	
}
