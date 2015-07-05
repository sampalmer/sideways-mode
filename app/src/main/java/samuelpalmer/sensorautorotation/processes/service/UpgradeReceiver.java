package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ServiceController;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ServiceController.updateFromSettings();
		new UserUpgrader().upgradeUser();
	}
}
