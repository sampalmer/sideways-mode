package samuelpalmer.sensorautorotation.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.UserUpgrader;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		new UserUpgrader().upgradeUser();
		new ServiceController().updateFromSettings();
	}

}
