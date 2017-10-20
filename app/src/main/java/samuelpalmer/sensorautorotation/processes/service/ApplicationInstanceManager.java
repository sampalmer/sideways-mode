package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public abstract class ApplicationInstanceManager {
	
	private static final String TAG = ApplicationInstanceManager.class.getSimpleName();
	
	public static final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private static boolean initialised = false;
	private static ApplicationInstance applicationInstance;
	private static AppDisabler appDisabler;
	
	public static void init() {
		if (initialised)
			throw new RuntimeException("Already initialised");
		
		appDisabler = new AppDisabler();
		
		ServiceRegistrar.instance.subscribe(updateApplicationState);
		ServiceSettings.instance().persistentState().subscribe(updateApplicationState);
		
		updateApplicationState.update();
		
		initialised = true;
	}
	
	private static final EventHandler updateApplicationState = new EventHandler() {
		@Override
		public void update() {
			if (ServiceSettings.instance().persistentState().hasValue())
				appDisabler.ensureStarted();
			else
				// No need to waste system resources monitoring the device's state while this app is turned off.
				appDisabler.ensureStopped();
			
			boolean isRunning = isApplicationRunning();
			boolean shouldBeRunning = applicationShouldBeRunning();
			
			if (shouldBeRunning != isRunning) {
				if (shouldBeRunning) {
					applicationInstance = new ApplicationInstance();
					applicationInstance.ensureStarted();
				}
				else {
					applicationInstance.ensureStopped();
					applicationInstance = null;
				}
				
				onChange.report();
			}
		}
	};
	
	public static boolean isApplicationRunning() {
		return applicationInstance != null;
	}
	
	public static ApplicationInstance applicationInstance() {
		if (!isApplicationRunning())
			throw new RuntimeException("Application is not running");

		return applicationInstance;
	}
	
	private static boolean applicationShouldBeRunning() {
		return
			!ServiceRegistrar.instance.getServiceInstances().isEmpty() // No point running the application if there are no services to keep it alive
			&&
			ServiceSettings.instance().persistentState().hasValue() // It shouldn't be running if it's turned off!
		;
	}
	
	public static void turnOffApp(String reason) {
		Log.i(TAG, "Turning off app. Reason: " + reason);
		ServiceSettings.instance().persistentState().remove();
		ServiceController.updateServiceState(false);
	}
	
}
