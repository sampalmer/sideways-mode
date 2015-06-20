package samuelpalmer.sensorautorotation.processes.service;

public class ToggleRotationLockReceiver extends ServiceReceiver {

	@Override
	protected void onReceive() {
		RotationLockToggler.toggle();
	}

}
