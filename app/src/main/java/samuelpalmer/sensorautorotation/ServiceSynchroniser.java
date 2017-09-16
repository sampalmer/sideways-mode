package samuelpalmer.sensorautorotation;

import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceSynchroniser {

	private final ApplicationSettings applicationSettings;

	public ServiceSynchroniser(ApplicationSettings applicationSettings) {
		this.applicationSettings = applicationSettings;
	}
	
	public void updateFromSettings() {
		ServiceController.updateServiceState(serviceEnabled());
	}

	public void enableService(UserSide userSide) {
		applicationSettings.enabled().set(true);
		
		Intent intent = ServiceController
			.intent()
			.putExtra(RotationService.EXTRA_INITIAL_USER_SIDE, userSide.name());
		
		ServiceController.ensureStarted(intent);
	}

	public void disableService() {
		applicationSettings.enabled().set(false);
		ServiceController.updateServiceState(false);
	}

	public boolean serviceEnabled() {
		return applicationSettings.enabled().get();
	}
	
}
