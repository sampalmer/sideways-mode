package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;

public class UpgradeReceiver extends UiReceiver {

	@Override
	protected void onReceive(Intent intent) {
		new UserUpgrader().upgradeUser();
	}

}
