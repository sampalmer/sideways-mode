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
		if (autoRotate == null)
			autoRotate = serviceSettings.autoRotate().get();
		return autoRotate;
	}

	private void setAutoRotate(boolean newValue) {
		serviceSettings.autoRotate().setAndCommit(newValue);
		autoRotate = newValue;
	}

	private Boolean autoRotate;

	public Orientation getUser() {
		if (user == null)
			user = new Orientation(serviceSettings.userOrientation().get());
		return user;
	}

	public void setUser(Orientation newValue) {
		serviceSettings.userOrientation().setAndCommit(serialise(newValue));
		user = newValue;
	}

	private Orientation user;

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().set(autoRotate, transaction);
		serviceSettings.userOrientation().set(serialise(user), transaction);
		transaction.commit();

		this.autoRotate = autoRotate;
		this.user = user;
	}

	public void clear() {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().remove(transaction);
		serviceSettings.userOrientation().remove(transaction);
		transaction.commit();

		this.autoRotate = null;
		this.user = null;
	}

	private int serialise(Orientation user) {
		if (user == null)
			throw new RuntimeException("No user orientation");

		return user.getClockwiseRotationsFromNaturalOrientation();
	}

}
