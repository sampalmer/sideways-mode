package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ToggleRotationLockReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		RotationLockToggler.updateAndToggle();
	}

}
