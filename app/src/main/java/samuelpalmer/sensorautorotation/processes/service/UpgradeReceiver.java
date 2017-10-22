package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * This receiver ensure the service process will be created when the app is upgraded.
 * This allows the process to restart the service straight away since the OS has killed it.
 */
public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(UpgradeReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
		
		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			boolean wasRunning = ServiceSettings.getSharedPreferences(CurrentProcess.context()).contains(ServiceSettings.KEY_PERSISTENT_STATE);
			
			// Only starting the service when absolutely necessary in case this is the cause of Android occasionally creating multiple simultaneous instances of the service.
			if (wasRunning) {
				// When Android updates an app, it kills the app's processes and doesn't restart the service.
				// So we need to restart it ourselves.
				ServiceController.ensureStarted();
			}
		}
		else
			Log.w(UpgradeReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
	}

}
