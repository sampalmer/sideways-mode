package samuelpalmer.sensorautorotation;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.components.RotationService;

public class RotationLockToggler {

	public void go() {
		if (RotationService.isRunning())
			RotationService.instance().toggleRotation();
		else
			showNotRunningMessage();
	}

	private void showNotRunningMessage() {
		Toast
				.makeText(ThisApplication.context(), StringResource.format(R.string.not_running_message, R.string.app_name), Toast.LENGTH_SHORT)
				.show();
	}

}
