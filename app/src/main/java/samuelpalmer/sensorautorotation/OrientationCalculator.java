package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator() {
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
		recalibrating = newValue;
	}

	private boolean recalibrating;

	public void setDevice(Orientation newDevice) {

		if (recalibrating) {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0)
				user = user.plus(deviceChange);
		}

		device = newDevice;
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
