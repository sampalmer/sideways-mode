package samuelpalmer.sensorautorotation;

import android.widget.Toast;

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
		recalibrating = true;
		applyRecalibration();
	}

	@Override
	protected void stop() {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
	}

	public boolean isRecalibrating() {
		return recalibrating;
	}

	public void toggleRecalibration() {
		recalibrating = !recalibrating;
		applyRecalibration();

		Toast
			.makeText(r, recalibrating ? "Screen rotation locked" : "Screen rotation enabled", Toast.LENGTH_SHORT)
			.show();
	}

	private void applyRecalibration() {
		r.promoteToForeground();
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

	public void resetUser() {
		user = DEFAULT;
		reportUpdate();
	}

}
