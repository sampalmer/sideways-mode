package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;

public abstract class RotationServiceClient {

	public static void send(String action) {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().handle(action); //This seems to have less lag than an intent
		else if (ServiceController.serviceEnabled())
			ServiceController.ensureStarted(toggleIntent(action));
		else
			showDisabledMessage();
	}

	private static Intent toggleIntent(String action) {
		return
			ServiceController
				.intent()
				.setAction(action);
	}

	private static void showDisabledMessage() {
		CharSequence message = StringResource.format(R.string.disabled_message, R.string.app_name);
		ToastReceiver.send(message);
	}

}
