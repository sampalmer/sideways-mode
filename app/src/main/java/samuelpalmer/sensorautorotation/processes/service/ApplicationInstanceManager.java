package samuelpalmer.sensorautorotation.processes.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.UserSide;

public abstract class ApplicationInstanceManager {
	
	private static final String TAG = ApplicationInstanceManager.class.getSimpleName();
	
	public static final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private static boolean initialised = false;
	private static ApplicationInstance applicationInstance;
	private static KeyguardManager keyguardManager;
	
	public static void init() {
		if (initialised)
			throw new RuntimeException("Already initialised");
		
		keyguardManager = (KeyguardManager) CurrentProcess.context().getSystemService(Context.KEYGUARD_SERVICE);
		
		ServiceRegistrar.instance.subscribe(updateApplicationState);
		ServiceSettings.instance().persistentState().subscribe(updateApplicationState);
		CurrentProcess.context().registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		CurrentProcess.context().registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		CurrentProcess.context().registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		if (Build.VERSION.SDK_INT >= 17) {
			CurrentProcess.context().registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_BACKGROUND));
			CurrentProcess.context().registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_FOREGROUND));
		}
		
		CurrentProcess.context().registerReceiver(unlockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
		
		initialised = true;
	}
	
	private static final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			disableService("Device is shutting down");
		}
		
	};
	
	private static final BroadcastReceiver screenChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isOnLockScreen = LockScreenMonitor.poll(keyguardManager);
			if (isOnLockScreen)
				disableService("Device is locked");
		}
	};
	
	private static final BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// We receive this broadcast after the device gets unlocked.
			// This implies that the device was locked, so the app shouldn't be on.
			// We need this check because Android doesn't broadcast when the device gets locked,
			// and our polling approach might sometimes be too slow to detect the lock screen.
			// For example, fingerprint unlock on my Motorola Moto G5 Plus unlocks the device
			// so quickly that you don't see the lock screen.
			disableService("Device was locked");
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
		Log.i(TAG, "Received command: " + command.action);
		
		String commandIgnoredReason = null;
		
		switch (command.action) {
			case ApplicationCommand.ACTION_LEFT_SIDE:
				if (ServiceSettings.instance().persistentState().hasValue())
					commandIgnoredReason = "Application is already enabled";
				else {
					ServiceSettings.instance().persistentState().set(UserSide.LEFT);
					ServiceController.updateServiceState(true);
				}
				break;
			case ApplicationCommand.ACTION_RIGHT_SIDE:
				if (ServiceSettings.instance().persistentState().hasValue())
					commandIgnoredReason = "Application is already enabled";
				else {
					ServiceSettings.instance().persistentState().set(UserSide.RIGHT);
					ServiceController.updateServiceState(true);
				}
				break;
			case ApplicationCommand.ACTION_CHANGE_SIDE:
				if (!isApplicationRunning())
					commandIgnoredReason = "Application is not running";
				else
					applicationInstance().changeSide();
				break;
			case ApplicationCommand.ACTION_TURN_OFF:
				if (!isApplicationRunning())
					commandIgnoredReason = "Application is not running";
				else
					disableService("Received application command");
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
		
		if (commandIgnoredReason != null)
			Log.w(TAG, "Ignored action " + command.action + ". Reason: " + commandIgnoredReason);
	}
	
	private static void disableService(String reason) {
		Log.i(TAG, "Turning off app. Reason: " + reason);
		ServiceSettings.instance().persistentState().remove();
		ServiceController.updateServiceState(false);
	}
}
