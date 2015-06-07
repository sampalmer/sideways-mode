package samuelpalmer.sensorautorotation.processes.service;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

abstract class RotationLockToggler {

	public static void toggle() {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().toggleRotation();
		else
			showNotRunningMessage();
	}

	private static void showNotRunningMessage() {
		Toast
				.makeText(Process.context(), StringResource.format(R.string.not_running_message, R.string.app_name), Toast.LENGTH_SHORT)
				.show();
	}

}
