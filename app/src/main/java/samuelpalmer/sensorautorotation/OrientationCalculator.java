package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator extends State {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final RotationService r;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(RotationService r) {
		this.r = r;
		listener = new UpdatingEventRegistrar<>();
	}

	@Override
	protected void start() {
	}

	@Override
	protected void stop() {
		device = DEFAULT;
		user = DEFAULT;
		r.setRecalibrating(true);
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
