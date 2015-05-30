package samuelpalmer.sensorautorotation;

import android.widget.Toast;

public abstract class RotationLockToggler {

	public static void toggle() {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().toggleRotation();
		else
			showNotRunningMessage();
	}

	private static void showNotRunningMessage() {
		Toast
				.makeText(ThisApplication.context(), StringResource.format(R.string.not_running_message, R.string.app_name), Toast.LENGTH_SHORT)
				.show();
	}

}
