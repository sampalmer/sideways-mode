package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.userinteractivity.UserInteractivityMonitor;

public abstract class ApplicationInstanceManager {
	
	private static final String TAG = ApplicationInstanceManager.class.getSimpleName();
	
	public static final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private static boolean initialised = false;
	private static ApplicationInstance applicationInstance;
	private static UserInteractivityMonitor userInteractivityMonitor;
	
	public static void init() {
		if (initialised)
			throw new RuntimeException("Already initialised");
		
		ServiceRegistrar.instance.subscribe(updateApplicationState);
		ServiceSettings.instance().persistentState().subscribe(updateApplicationState);
		CurrentProcess.context().registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		
		userInteractivityMonitor = new UserInteractivityMonitor();
		userInteractivityMonitor.subscribe(userInteractivityListener);
		userInteractivityListener.update();
		
		initialised = true;
	}
	
	private static final EventHandler userInteractivityListener = new EventHandler() {
		@Override
		public void update() {
			if (!userInteractivityMonitor.get().active()) {
				disableService();
			}
		}
	};
	
	private static final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			disableService();
		}
		
	};
	
	private static final EventHandler updateApplicationState = new EventHandler() {
		@Override
		public void update() {
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
	
	public static void handle(ApplicationCommand command) {
		// TODO: Do proper validation of application enabled state and running state here.
		
		Log.i(TAG, "Received command: " + command.action);
		
		switch (command.action) {
			case ApplicationCommand.ACTION_LEFT_SIDE:
				ServiceSettings.instance().persistentState().set(UserSide.LEFT);
				ServiceController.updateServiceState(true);
				break;
			case ApplicationCommand.ACTION_RIGHT_SIDE:
				ServiceSettings.instance().persistentState().set(UserSide.RIGHT);
				ServiceController.updateServiceState(true);
				break;
			case ApplicationCommand.ACTION_CHANGE_SIDE:
				applicationInstance().changeSide();
				break;
			case ApplicationCommand.ACTION_TURN_OFF:
				disableService();
				break;
			case ApplicationCommand.ACTION_SYNC_SERVICE:
				ServiceController.updateServiceState(ServiceSettings.instance().persistentState().hasValue());
				break;
			case ApplicationCommand.ACTION_CLEAR_ERROR:
				EnvironmentErrorManager.clear();
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}
	}
	
	private static void disableService() {
		ServiceSettings.instance().persistentState().remove();
		ServiceController.updateServiceState(false);
	}
}
