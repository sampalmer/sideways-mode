package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();

		if (!serviceSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			setAutoRotate(accelerometerRotation);
		}

		if (!serviceSettings.userOrientation().hasValue())
			setUser(Orientation.NATURAL);
	}

	public boolean getAutoRotate() {
		return serviceSettings.autoRotate().get();
	}

	private void setAutoRotate(boolean newValue) {
		serviceSettings.autoRotate().setAndCommit(newValue);
	}

	public Orientation getUser() {
		return new Orientation(serviceSettings.userOrientation().get());
	}

	public void setUser(Orientation newValue) {
		serviceSettings.userOrientation().setAndCommit(serialise(newValue));
	}

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().set(autoRotate, transaction);
		serviceSettings.userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	public void clear() {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().remove(transaction);
		serviceSettings.userOrientation().remove(transaction);
		transaction.commit();
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
