package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceController;
import samuelpalmer.sensorautorotation.processes.service.ServiceRunningStateHolder;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.UserSide;

public class ApplicationCommandReceiver extends BroadcastReceiver {
	
	private static final String TAG = ApplicationCommandReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ApplicationCommand command = new ApplicationCommand(intent);
		Log.i(TAG, "Received command: " + command.action);
		
		if (command.forcedOrientationAppPackages != null) {
			Log.i(TAG, "Command included config. Applying new config.");
			
			ServiceSettings.getSharedPreferences(context)
				.edit()
				.putStringSet(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, command.forcedOrientationAppPackages)
				.apply();
		}
		
		switch (command.action) {
			case ApplicationCommand.ACTION_LEFT_SIDE:
				setUserSide(UserSide.LEFT);
				collapseNotificationShade(context);
				
				break;
			case ApplicationCommand.ACTION_RIGHT_SIDE:
				setUserSide(UserSide.RIGHT);
				collapseNotificationShade(context);
				
				break;
			case ApplicationCommand.ACTION_TURN_OFF:
				turnOffApp("Received application command");
				collapseNotificationShade(context);
				
				break;
			case ApplicationCommand.ACTION_CLEAR_ERROR:
				EnvironmentErrorManager.clear();
				break;
			case ApplicationCommand.ACTION_SYNC_CONFIG:
				if (command.forcedOrientationAppPackages == null)
					throw new RuntimeException("Received request to sync config, but no config was provided.");
				
				// No need to sync the config here; it is handled elsewhere.
				
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}
	}
	
	// It would be nice to move this logic into the service as an intent action, but maybe that could contribute to the OS starting multiple simultaneous instances of the service?
	public static void turnOffApp(String reason) {
		Log.i(TAG, "Turning off app. Reason: " + reason);
		
		if (ServiceSettings.getSharedPreferences(CurrentProcess.context()).contains(ServiceSettings.KEY_PERSISTENT_STATE))
			ServiceController.ensureStopped();
		else {
			// No state means service has not started and is not enabled.
			// We could stop the service here just in case the OS considers it "started" but hasn't yet started it.
			// But maybe that could contribute to the OS starting multiple simultaneous instances of the service?
			
			Log.w(TAG, "Ignoring request to turn off app since it is already off");
		}
	}
	
	// It would be nice to move this logic into the service as an intent action, but maybe that could contribute to the OS starting multiple simultaneous instances of the service?
	private void setUserSide(UserSide userSide) {
		if (ServiceRunningStateHolder.isServiceRunning())
			ServiceRunningStateHolder.getServiceInstance().setSide(userSide);
		else {
			ServiceSettings
				.getSharedPreferences(CurrentProcess.context())
				.edit()
				.putString(ServiceSettings.KEY_PERSISTENT_STATE, userSide.name())
				.apply();
			
			// Only starting the service when absolutely necessary in case this is the cause of Android occasionally creating multiple simultaneous instances of the service.
			// It's possible for the service to not be running but still have its state. Examples:
			// * User force stops the app while the service is running.
			// * The service crashed and the OS didn't restart it.
			// * User's device dies while the service is running.
			// * The OS ran extremely low on memory and killed the service process while it was running.
			ServiceController.ensureStarted();
		}
	}
	
	private static void collapseNotificationShade(Context context) {
		// The user might be in a hurry and want to quickly press a notification button and then return to their task.
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}
	
}
