package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ApplicationSettings applicationSettings;

	public ApplicationState(ApplicationSettings applicationSettings, SystemSettings systemSettings) {
		this.applicationSettings = applicationSettings;

		if (!applicationSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate(accelerometerRotation);
		}

		if (!applicationSettings.userOrientation().hasValue())
			user(Orientation.NATURAL);
	}

	public boolean autoRotate() {
		return applicationSettings.autoRotate().get();
	}

	public Orientation user() {
		return new Orientation(applicationSettings.userOrientation().get());
	}

	public void set(boolean autoRotate, Orientation user) {
		ApplicationSettings.Transaction transaction = applicationSettings.new Transaction();
		applicationSettings.autoRotate().set(autoRotate, transaction);
		applicationSettings.userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	private void autoRotate(boolean newValue) {
		applicationSettings.autoRotate().setAndCommit(newValue);
	}

	public void user(Orientation newValue) {
		applicationSettings.userOrientation().setAndCommit(serialise(newValue));
	}

	public void clear() {
		ApplicationSettings.Transaction transaction = applicationSettings.new Transaction();
		applicationSettings.autoRotate().remove(transaction);
		applicationSettings.userOrientation().remove(transaction);
		transaction.commit();
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
