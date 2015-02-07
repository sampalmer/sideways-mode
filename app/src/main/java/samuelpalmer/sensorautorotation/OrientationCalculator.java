package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final ScreenOrientationController screen;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	private boolean deviceSpecified;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(ScreenOrientationController screen) {
		this.screen = screen;
		listener = new UpdatingEventRegistrar<>();
	}

	public void reset() {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
	}

	public boolean isRecalibrating() {
		return recalibrating;
	}

	public void setRecalibrating(boolean newValue) {
		if (newValue) {
			inferUserFromScreen();
			reportUpdate();
		}

		recalibrating = newValue;
	}

	private void inferUserFromScreen() {
		user = Orientation.NATURAL.plus(device.minus(screen.realOrientation()));
	}

	private boolean recalibrating;

	public void setDevice(Orientation newDevice) {

		if (recalibrating) {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0)
				user = user.plus(deviceChange);
		}

		device = newDevice;

		if (!deviceSpecified) {
			inferUserFromScreen();
			deviceSpecified = true;
		}

		reportUpdate();

	}

	private void reportUpdate() {
		listener.report(null);
	}

	public EventRegistrar<Void> onChange() {
		return listener;
	}

	public Orientation proposedScreenOrientation() {
		return device.plus(userOffset());
	}

	private Rotation userOffset() {
		return Orientation.NATURAL.minus(user);
	}

}
