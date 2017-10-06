package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(UpgradeReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());

		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))
			// When Android updates an app, it kills the app's process and doesn't restart them.
			// So we need to restart the app ourselves.
			ServiceController.updateServiceState(ServiceSettings.instance().persistentState().hasValue());
		else
			Log.w(UpgradeReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
	}

}
