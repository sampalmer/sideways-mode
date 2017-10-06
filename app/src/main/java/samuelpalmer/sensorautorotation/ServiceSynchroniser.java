package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public abstract class ServiceSynchroniser {
	
	public static void updateFromSettings() {
		ServiceController.updateServiceState(ServiceSettings.instance().persistentState().hasValue());
	}

	public static void enableService(UserSide userSide) {
		ServiceSettings.instance().persistentState().set(userSide);
		ServiceController.updateServiceState(true);
	}

	public static void disableService() {
		ServiceSettings.instance().persistentState().remove();
		ServiceController.updateServiceState(false);
	}
	
}
