package samuelpalmer.sensorautorotation.processes.service;

public class UpgradeReceiver extends ServiceReceiver {
	@Override
	protected void onReceive() {
		new UserUpgrader().upgradeUser();
	}
}
