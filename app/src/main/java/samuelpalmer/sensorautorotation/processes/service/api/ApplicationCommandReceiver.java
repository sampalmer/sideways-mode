package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceController;
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
			ServiceSettings.instance().forcedOrientationAppPackages().set(command.forcedOrientationAppPackages);
		}
		
		String commandIgnoredReason = null;
		
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
				if (!ApplicationInstanceManager.isApplicationRunning())
					commandIgnoredReason = "Application is not running";
				else
					ApplicationInstanceManager.turnOffApp("Received application command");
				
				collapseNotificationShade(context);
				
				break;
			case ApplicationCommand.ACTION_SYNC_SERVICE:
				if (!ServiceSettings.instance().persistentState().hasValue())
					commandIgnoredReason = "Application is turned off";
				else if (ApplicationInstanceManager.isApplicationRunning())
					commandIgnoredReason = "Application is already running";
				else
					ServiceController.updateServiceState(true);
				
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
		
		if (commandIgnoredReason != null)
			Log.w(TAG, "Ignored action " + command.action + ". Reason: " + commandIgnoredReason);
	}
	
	private void setUserSide(UserSide userSide) {
		if (ServiceSettings.instance().persistentState().hasValue()) {
			if (ApplicationInstanceManager.isApplicationRunning())
				ApplicationInstanceManager.applicationInstance().setSide(userSide);
			else
				ServiceSettings.instance().persistentState().set(userSide);
		}
		else {
			ServiceSettings.instance().persistentState().set(userSide);
			
			// Only starting the service when absolutely necessary in case this is the cause of Android occasionally creating multiple simultaneous instances of the service.
			ServiceController.updateServiceState(true);
		}
	}
	
	private static void collapseNotificationShade(Context context) {
		// The user might be in a hurry and want to quickly press a notification button and then return to their task.
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}
	
}
