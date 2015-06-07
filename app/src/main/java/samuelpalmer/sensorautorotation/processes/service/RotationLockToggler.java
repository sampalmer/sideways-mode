package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;

abstract class RotationLockToggler {

	public static void toggle() {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().toggleRotation();
		else
			showNotRunningMessage();
	}

	private static void showNotRunningMessage() {
		CharSequence message = StringResource.format(R.string.not_running_message, R.string.app_name);
		ToastReceiver.send(message);
	}

}
