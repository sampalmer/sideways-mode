package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Transaction;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	public ApplicationState(SystemSettings systemSettings) {
		if (!ApplicationSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate(accelerometerRotation);
		}

		if (!ApplicationSettings.userOrientation().hasValue())
			user(Orientation.NATURAL);
	}

	public boolean autoRotate() {
		return ApplicationSettings.autoRotate().get();
	}

	public Orientation user() {
		return new Orientation(ApplicationSettings.userOrientation().get());
	}

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = new Transaction();
		ApplicationSettings.autoRotate().set(autoRotate, transaction);
		ApplicationSettings.userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	private void autoRotate(boolean newValue) {
		ApplicationSettings.autoRotate().setAndCommit(newValue);
	}

	public void user(Orientation newValue) {
		ApplicationSettings.userOrientation().setAndCommit(serialise(newValue));
	}

	public void clear() {
		Transaction transaction = new Transaction();
		ApplicationSettings.autoRotate().remove(transaction);
		ApplicationSettings.userOrientation().remove(transaction);
		transaction.commit();
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
