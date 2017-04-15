package samuelpalmer.sensorautorotation.processes.ui;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.CurrentProcess;

public abstract class Toaster {

	private static Toast statusToast;

	public static void show(CharSequence text, int duration) {
		if (statusToast != null) {
			statusToast.setText(text);
			statusToast.setDuration(duration);
		}
		else
			statusToast = Toast.makeText(CurrentProcess.context(), text, duration);

		statusToast.show();
	}

}
