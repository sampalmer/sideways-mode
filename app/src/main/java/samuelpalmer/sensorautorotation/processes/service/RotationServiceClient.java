package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class RotationServiceClient {

	public static void send(RotationServiceCommand command) {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().handle(command); //This seems to have less lag than an intent
		else if (new ServiceSynchroniser(ApplicationSettings.local()).serviceEnabled())
			ServiceController.ensureStarted(command.toIntent());
		else
			showDisabledMessage();
	}

	public static void showDisabledMessage() {
		CharSequence message = disabledMessage();
		ToastReceiver.send(message);
	}

	public static CharSequence disabledMessage() {
		return StringResource.format(R.string.app_disabled_message, R.string.app_name_full);
	}

}
