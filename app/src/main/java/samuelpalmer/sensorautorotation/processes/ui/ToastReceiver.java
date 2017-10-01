package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.BroadcastKillFix;
import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * Handles requests to show toasts. Displays them using the main process since displaying toasts
 * seems to increase the process's RAM usage by ~10MB.
 */
public class ToastReceiver extends BroadcastReceiver {

	private static final String EXTRA_MESSAGE = null; //This key is null for backwards compatibility
	private static final String EXTRA_DURATION = "duration";

	public static void send(CharSequence message, int duration) {
		Intent intent = new Intent(CurrentProcess.context(), ToastReceiver.class)
				.putExtra(EXTRA_MESSAGE, message)
				.putExtra(EXTRA_DURATION, duration);
		BroadcastKillFix.apply(intent);
		
		CurrentProcess.context().sendBroadcast(intent);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Toaster.show(
				intent.getCharSequenceExtra(EXTRA_MESSAGE),
				intent.getIntExtra(EXTRA_DURATION, Toast.LENGTH_SHORT)
		);
	}
}
