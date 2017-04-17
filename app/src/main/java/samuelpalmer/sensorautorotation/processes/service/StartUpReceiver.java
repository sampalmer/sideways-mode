package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class StartUpReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(StartUpReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());

		if (
			Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
			||
			Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())
		)
			new ServiceSynchroniser(ApplicationSettings.local()).updateFromSettings();
		else
			Log.w(StartUpReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
	}

}
