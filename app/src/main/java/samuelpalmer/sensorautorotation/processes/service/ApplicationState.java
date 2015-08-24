package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();

		if (!serviceSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate(accelerometerRotation);
		}

		if (!serviceSettings.userOrientation().hasValue())
			user(Orientation.NATURAL);
	}

	public boolean autoRotate() {
		return serviceSettings.autoRotate().get();
	}

	public Orientation user() {
		return new Orientation(serviceSettings.userOrientation().get());
	}

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().set(autoRotate, transaction);
		serviceSettings.userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	private void autoRotate(boolean newValue) {
		serviceSettings.autoRotate().setAndCommit(newValue);
	}

	public void user(Orientation newValue) {
		serviceSettings.userOrientation().setAndCommit(serialise(newValue));
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
