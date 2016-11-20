package samuelpalmer.sensorautorotation.processes.ui;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.CurrentProcess;

abstract class Toaster {

	private static Toast statusToast;

	public static void show(CharSequence text) {
		if (statusToast != null)
			statusToast.setText(text);
		else
			statusToast = Toast.makeText(CurrentProcess.context(), text, Toast.LENGTH_SHORT);

		statusToast.show();
	}

}
