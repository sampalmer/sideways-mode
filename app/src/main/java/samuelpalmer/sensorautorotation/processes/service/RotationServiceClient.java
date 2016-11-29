package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class RotationServiceClient {

	public static void send(String action) {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().handle(action); //This seems to have less lag than an intent
		else if (new ServiceSynchroniser(ApplicationSettings.local()).serviceEnabled())
			ServiceController.ensureStarted(makeIntent(action));
		else
			showDisabledMessage();
	}

	private static Intent makeIntent(String action) {
		return
			ServiceController
				.intent()
				.setAction(action);
	}

	public static void showDisabledMessage() {
		CharSequence message = StringResource.format(R.string.app_disabled_message, R.string.app_name_full);
		ToastReceiver.send(message);
	}

}
