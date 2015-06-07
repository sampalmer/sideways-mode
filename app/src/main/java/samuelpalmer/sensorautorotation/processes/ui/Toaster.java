package samuelpalmer.sensorautorotation.processes.ui;

import android.widget.Toast;

public abstract class Toaster {

	private static Toast statusToast;

	public static void show(CharSequence text) {
		if (statusToast != null)
			statusToast.setText(text);
		else
			statusToast = Toast.makeText(samuelpalmer.sensorautorotation.Process.context(), text, Toast.LENGTH_SHORT);

		statusToast.show();
	}

}
