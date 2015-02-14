package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class ApplicationState {

	private final ApplicationSettings settings;

	public ApplicationState(ApplicationSettings settings, SystemSettings systemSettings) {
		this.settings = settings;

		if (!settings.autoRotate().hasValue()) {
			Boolean accelerometerRotation = systemSettings.accelerometerRotation.getOrNull();
			autoRotate(accelerometerRotation != null && accelerometerRotation);
		}

		if (!settings.userOrientation().hasValue())
			user(Orientation.NATURAL);
	}

	public boolean autoRotate() {
		return settings.autoRotate().get();
	}

	public Orientation user() {
		return new Orientation(settings.userOrientation().get());
	}

	public void set(boolean autoRotate, Orientation user) {
		ApplicationSettings.Transaction transaction = settings.new Transaction();
		settings.autoRotate().set(autoRotate, transaction);
		settings.userOrientation().set(serialise(user), transaction);
		transaction.commit();
	}

	public void autoRotate(boolean newValue) {
		settings.autoRotate().setAndCommit(newValue);
	}

	public void user(Orientation newValue) {
		settings.userOrientation().setAndCommit(serialise(newValue));
	}

	public void clear() {
		ApplicationSettings.Transaction transaction = settings.new Transaction();
		settings.autoRotate().remove(transaction);
		settings.userOrientation().remove(transaction);
		transaction.commit();
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
