package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class RotationLockToggler {

	public static void updateAndToggle() {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().toggleRotation(); //This seems to have less lag than an intent
		else if (enabled())
			ServiceController.ensureStarted(toggleIntent());
		else
			showDisabledMessage();
	}

	private static Intent toggleIntent() {
		return
			ServiceController.intent()
			.setAction(RotationService.ACTION_TOGGLE);
	}

	private static Boolean enabled() {
		return new ApplicationSettings().enabled().get();
	}

	private static void showDisabledMessage() {
		CharSequence message = StringResource.format(R.string.disabled_message, R.string.app_name);
		ToastReceiver.send(message);
	}

}
