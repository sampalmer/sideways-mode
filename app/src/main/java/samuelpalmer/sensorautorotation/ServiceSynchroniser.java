package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceSynchroniser {

	private final ApplicationSettings applicationSettings;

	public ServiceSynchroniser(ApplicationSettings applicationSettings) {
		this.applicationSettings = applicationSettings;
	}
	
	public void updateFromSettings() {
		ServiceController.updateServiceState(serviceEnabled());
	}

	public void enableService() {
		setEnabled(true);
	}

	public void disableService() {
		setEnabled(false);
	}

	private void setEnabled(boolean value) {
		applicationSettings.enabled().setAndCommit(value);
		ServiceController.updateServiceState(value);
	}

	public boolean serviceEnabled() {
		return applicationSettings.enabled().get();
	}
	
}
