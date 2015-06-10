package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

abstract class UiReceiver extends BroadcastReceiver {
	@Override
	public final void onReceive(Context context, Intent intent) {
		UiProcess.ensureInitialised();
		onReceive(intent);
	}

	protected abstract void onReceive(Intent intent);

}
