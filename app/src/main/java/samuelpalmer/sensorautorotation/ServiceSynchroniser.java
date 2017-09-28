package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public abstract class ServiceSynchroniser {
	
	public static void updateFromSettings() {
		ServiceController.updateServiceState(serviceEnabled());
	}

	public static void enableService(UserSide userSide) {
		ServiceSettings.instance().persistentState().set(userSide);
		ServiceController.updateServiceState(true);
	}

	public static void disableService() {
		ServiceSettings.instance().persistentState().remove();
		ServiceController.updateServiceState(false);
	}

	public static boolean serviceEnabled() {
		return ServiceSettings.instance().persistentState().hasValue();
	}
	
}
