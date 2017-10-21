package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.OrientationEnforcementManager;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityMonitor;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;
import samuelpalmer.sensorautorotation.values.UserSide;

public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {
	
	private static final String TAG = RotationService.class.getSimpleName();
	
	private OrientationCalculator calculator;
	ApplicationStatusDisplayUpdater applicationStatusDisplayUpdater;
	private ServiceRevivalManager serviceRevivalManager;
	private OrientationSensorHandler orientationSensorHandler;
	private SystemAutoRotationMonitor systemAutoRotationMonitor;
	private OrientationEnforcementManager orientationEnforcementManager;
	ActivityMonitor activityMonitor;
	private ScreenOrientationUpdater screenUpdater;
	private UserInteractivityMonitor userInteractivityMonitor;
	private AppDisabler appDisabler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (ServiceHolder.isServiceRunning()) {
			if (ServiceHolder.getServiceInstance() == this) {
				// This is weird, but this kind of thing pops in in Production every now and then.
				Log.w(TAG, "Service onCreate() was called a second time on the same instance.");
				return; // The service was already initialised, so no need to do so again.
			}
			else {
				// This is weird, but it actually happens occasionally in Production :(
				throw new RuntimeException("Service onCreate() called on a different instance of the service.");
			}
		}
		
		Log.i(TAG, "Service is starting");
		
		//This should happen before other work is done so it doesn't clear any errors that occur during initialisation.
		EnvironmentErrorManager.clear();
		
		SystemSettings systemSettings = new SystemSettings();
		ScreenRotationMonitor screenRotationMonitor = new ScreenRotationMonitor();
		calculator = new OrientationCalculator(screenRotationMonitor);
		screenUpdater = new ScreenOrientationUpdater(calculator, systemSettings, screenRotationMonitor);
		applicationStatusDisplayUpdater = new ApplicationStatusDisplayUpdater(calculator, this);
		serviceRevivalManager = new ServiceRevivalManager();
		orientationSensorHandler = new OrientationSensorHandler(calculator, screenRotationMonitor);
		systemAutoRotationMonitor = new SystemAutoRotationMonitor(systemSettings);
		activityMonitor = new ActivityMonitor();
		orientationEnforcementManager = new OrientationEnforcementManager(activityMonitor);
		appDisabler = new AppDisabler();
		
		systemAutoRotationMonitor.ensureStarted();
		applicationStatusDisplayUpdater.ensureStarted();
		screenUpdater.ensureStarted();
		serviceRevivalManager.ensureStarted();
		activityMonitor.ensureStarted();
		orientationEnforcementManager.ensureStarted();
		appDisabler.ensureStarted();
		
		userInteractivityMonitor = new UserInteractivityMonitor();
		userInteractivityMonitor.subscribe(userInteractivityListener);
		userInteractivityListener.update();
		
		ServiceHolder.serviceStarted(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (!ServiceHolder.isServiceRunning()) {
			Log.w(TAG, "Service onDestroy() was called, but this instance of the service is already destroyed.");
			return;
		}
		
		if (ServiceHolder.getServiceInstance() != this)
			// This is weird, but this kind of thing actually happens occasionally in Production :(
			throw new RuntimeException("Service onDestroy() called on a different instance of the service.");
		
		ServiceHolder.serviceStopped();
		
		Log.i(TAG, "Service is stopping");
		
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
