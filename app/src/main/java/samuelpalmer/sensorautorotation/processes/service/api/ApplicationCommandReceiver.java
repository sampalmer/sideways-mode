package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceController;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;

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
				if (ServiceSettings.instance().persistentState().hasValue() && ApplicationInstanceManager.isApplicationRunning()) {
					ApplicationInstanceManager.applicationInstance().setSide(UserSide.LEFT);
				}
				else {
					ServiceSettings.instance().persistentState().set(UserSide.LEFT);
					ServiceController.updateServiceState(true);
				}
				
				collapseNotificationShade(context);
				
				break;
			case ApplicationCommand.ACTION_RIGHT_SIDE:
				if (ServiceSettings.instance().persistentState().hasValue() && ApplicationInstanceManager.isApplicationRunning())
					ApplicationInstanceManager.applicationInstance().setSide(UserSide.RIGHT);
				else {
					ServiceSettings.instance().persistentState().set(UserSide.RIGHT);
					ServiceController.updateServiceState(true);
				}
				
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
				ServiceController.updateServiceState(ServiceSettings.instance().persistentState().hasValue());
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
	
	private static void collapseNotificationShade(Context context) {
		// The user might be in a hurry and want to quickly press a notification button and then return to their task.
		context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}
	
}
