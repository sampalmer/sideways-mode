package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(UpgradeReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());

		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))
			// When Android updates an app, it kills the app's process and doesn't restart them.
			// So we need to restart the app ourselves.
			ServiceSynchroniser.updateFromSettings();
		else
			Log.w(UpgradeReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
	}

}
