package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final Screen screen;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	private boolean deviceSpecified;
	private boolean recalibrating;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(Screen screen, SystemSettings settings) {
		this.screen = screen;
		listener = new UpdatingEventRegistrar<>();

		Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
		recalibrating = accelerometerRotation == null || !accelerometerRotation;
	}

	public void reset() {
		device = DEFAULT;
		user = DEFAULT;
		recalibrating = true;
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
