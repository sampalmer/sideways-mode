package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public abstract class ServiceSynchroniser {
	
	public static void updateFromSettings() {
		ServiceController.updateServiceState(ServiceSettings.instance().persistentState().hasValue());
	}
	
}
