package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * This is separate from {@link UpgradeReceiver} so that {@link UpgradeReceiver} can be
 * exported without being open to broadcasts from other apps.
 */
public class ServiceSyncReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ServiceSynchroniser.updateFromSettings();
	}

}
