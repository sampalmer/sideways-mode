package samuelpalmer.sensorautorotation.processes.service.api;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.processes.ui.ToastReceiver;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public abstract class ApplicationInstanceClient {

	public static void send(ApplicationInstanceCommand command) {
		if (ApplicationInstanceManager.isApplicationRunning())
			ApplicationInstanceManager.applicationInstance().handle(command); //This seems to have less lag than an intent
		else if (ServiceSettings.instance().persistentState().hasValue())
			ServiceController.ensureStarted(command.toIntent());
		else
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
			return StringResource.format(R.string.app_disabled_because_of_error, R.string.app_name) + " " + StringResource.get(R.string.app_disabled_view_error_instruction);
		else
			return StringResource.format(R.string.app_disabled_message, R.string.app_name);
	}

}
