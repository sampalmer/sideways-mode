package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceStateUpdatingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		new ServiceSynchroniser(ApplicationSettings.local()).updateFromSettings();
	}

}
