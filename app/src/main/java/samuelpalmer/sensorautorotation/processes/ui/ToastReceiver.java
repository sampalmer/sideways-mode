package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;

import samuelpalmer.sensorautorotation.Process;

/**
 * Handles requests to show toasts. Displays them using the main process since displaying toasts
 * seems to increase the process's RAM usage by ~10MB.
 */
public class ToastReceiver extends UiReceiver {

	private static final String EXTRA_MESSAGE = null;

	public static void send(CharSequence message) {
		Intent intent = new Intent(Process.context(), ToastReceiver.class)
				.putExtra(EXTRA_MESSAGE, message);
		Process.context().sendBroadcast(intent);
	}

	@Override
	protected void onReceive(Intent intent) {
		Toaster.show(intent.getCharSequenceExtra(EXTRA_MESSAGE));
	}

}
