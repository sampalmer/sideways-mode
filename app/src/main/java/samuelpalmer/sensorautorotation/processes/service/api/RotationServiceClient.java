package samuelpalmer.sensorautorotation.processes.service.api;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.service.ServiceHolder;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class RotationServiceClient {

	public static void send(RotationServiceCommand command) {
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().handle(command); //This seems to have less lag than an intent
		else if (new ServiceSynchroniser(ApplicationSettings.local()).serviceEnabled())
			ServiceController.ensureStarted(command.toIntent());
		else if (command.toastIfServiceDisabled)
			showDisabledMessage();
	}

	private static void showDisabledMessage() {
		CharSequence message = disabledMessage();
		ToastReceiver.send(message, Toast.LENGTH_LONG);
	}

	private static CharSequence disabledMessage() {
		EnvironmentError lastError = EnvironmentError.lastError();

		if (lastError != null)
			/*
				Not showing the user any error details here because it's important that they see *all* error information and actions so that the user will be less likely to blame the developer and send useless error reports, which has been an ongoing problem.
			 */
			return StringResource.format(R.string.app_disabled_because_of_error, R.string.app_name_full) + " " + StringResource.get(R.string.app_disabled_view_error_instruction);
		else
			return StringResource.format(R.string.app_disabled_message, R.string.app_name_full);
	}

}
