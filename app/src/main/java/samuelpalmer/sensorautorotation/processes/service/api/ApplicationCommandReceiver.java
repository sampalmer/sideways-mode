package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Collections;

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
		
		if (command.config != null) {
			Log.i(TAG, "Command included config. Applying new config.");

			SharedPreferences serviceSettings = ServiceSettings.getSharedPreferences(context);
			SharedPreferences.Editor editor = serviceSettings.edit();

			if (!command.config.incompatibleAppPackages.equals(serviceSettings.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet())))
				editor.putStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, command.config.incompatibleAppPackages);

			if (command.config.uninstallingTipShown != serviceSettings.getBoolean(ServiceSettings.KEY_UNINSTALLING_TIP_SHOWN, false))
				editor.putBoolean(ServiceSettings.KEY_UNINSTALLING_TIP_SHOWN, command.config.uninstallingTipShown);

			editor.apply();
		}
		
		switch (command.action) {
			case ApplicationCommand.ACTION_LEFT_SIDE:
				setUserSide(context, UserSide.LEFT);
				break;
			case ApplicationCommand.ACTION_RIGHT_SIDE:
				setUserSide(context, UserSide.RIGHT);
				break;
			case ApplicationCommand.ACTION_TURN_OFF:
				turnOffApp(context, "Received application command");
				break;
			case ApplicationCommand.ACTION_CLEAR_ERROR:
				EnvironmentErrorManager.clear(context);
				break;
			case ApplicationCommand.ACTION_SYNC_CONFIG:
				if (command.config == null)
					throw new RuntimeException("Received request to sync config, but no config was provided.");
				
				// No need to sync the config here; it is handled elsewhere.
				
				break;
			default:
				throw new IllegalArgumentException("Unknown action: " + command.action);
		}
	}
	
	// It would be nice to move this logic into the service as an intent action, but maybe that could contribute to the OS starting multiple simultaneous instances of the service?
	public static void turnOffApp(Context context, String reason) {
		Log.i(TAG, "Turning off app. Reason: " + reason);
		
		if (ServiceSettings.getSharedPreferences(context).contains(ServiceSettings.KEY_PERSISTENT_STATE))
			ServiceController.ensureStopped(context);
		else {
			// No state means service has not started and is not enabled.
			// We could stop the service here just in case the OS considers it "started" but hasn't yet started it.
			// But maybe that could contribute to the OS starting multiple simultaneous instances of the service?
			
			Log.w(TAG, "Ignoring request to turn off app since it is already off");
		}
	}
	
	// It would be nice to move this logic into the service as an intent action, but maybe that could contribute to the OS starting multiple simultaneous instances of the service?
	private void setUserSide(Context context, UserSide userSide) {
		if (ServiceRunningStateHolder.isServiceRunning())
			ServiceRunningStateHolder.getServiceInstance().setSide(userSide);
		else {
			SharedPreferences prefs = ServiceSettings.getSharedPreferences(context);
			
			if (prefs.contains(ServiceSettings.KEY_PERSISTENT_STATE))
				Log.w(TAG, "Service is not running but has persistent state, so the app may not have shut down correctly.");
			
			prefs
				.edit()
				.putString(ServiceSettings.KEY_PERSISTENT_STATE, userSide.name())
				.apply();
			
			// Only starting the service when absolutely necessary in case this is the cause of Android occasionally creating multiple simultaneous instances of the service.
			// It's possible for the service to not be running but still have its state. Examples:
			// * User force stops the app while the service is running.
			// * The service crashed and the OS didn't restart it.
			// * User's device dies while the service is running.
			// * The OS ran extremely low on memory and killed the service process while it was running.
			ServiceController.ensureStarted(context);
		}
	}
	
}
