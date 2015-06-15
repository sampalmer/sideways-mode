package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

abstract class ServiceReceiver extends BroadcastReceiver {
	@Override
	public final void onReceive(Context context, Intent intent) {
		ServiceProcess.ensureInitialised();
		onReceive(intent);
	}

	protected abstract void onReceive(Intent intent);

}
