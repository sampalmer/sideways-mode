package samuelpalmer.sensorautorotation.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.RotationLockToggler;

public class ToggleRotationLockReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		new RotationLockToggler().go();
	}
}
