package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	public ApplicationState(SystemSettings systemSettings) {
		if (!ApplicationSettings.instance().autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate(accelerometerRotation);
		}

		if (!ApplicationSettings.instance().userOrientation().hasValue())
			user(Orientation.NATURAL);
	}

	public boolean autoRotate() {
		return ApplicationSettings.instance().autoRotate().get();
	}

	public Orientation user() {
		return new Orientation(ApplicationSettings.instance().userOrientation().get());
	}

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = ApplicationSettings.instance().new Transaction();
		ApplicationSettings.instance().autoRotate().set(autoRotate, transaction);
		ApplicationSettings.instance().userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	private void autoRotate(boolean newValue) {
		ApplicationSettings.instance().autoRotate().setAndCommit(newValue);
	}

	public void user(Orientation newValue) {
		ApplicationSettings.instance().userOrientation().setAndCommit(serialise(newValue));
	}

	public void clear() {
		Transaction transaction = ApplicationSettings.instance().new Transaction();
		ApplicationSettings.instance().autoRotate().remove(transaction);
		ApplicationSettings.instance().userOrientation().remove(transaction);
		transaction.commit();
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
