package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Handles requests to show toasts. Displays them using the main process since displaying toasts
 * seems to increase the process's RAM usage by ~10MB.
 */
public class ToastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Toaster.show(intent.getCharSequenceExtra(null));
	}
}
