package samuelpalmer.sensorautorotation.processes.service.api;

import android.util.Log;

import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public abstract class ApplicationInstanceClient {

	public static void send(ApplicationInstanceCommand command) {
		if (ApplicationInstanceManager.isApplicationRunning())
			ApplicationInstanceManager.applicationInstance().handle(command); //This seems to have less lag than an intent
		else if (ServiceSettings.instance().persistentState().hasValue())
			ServiceController.ensureStarted(command.toIntent());
		else {
			// This shouldn't really happen, but it might occur if the service gets turned off in between when the command is sent and when it's received.
			Log.w(ApplicationInstanceClient.class.getSimpleName(), "Received command, but the service is not turned on");
		}
	}
	
}
