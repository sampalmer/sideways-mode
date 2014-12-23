package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Vibrator;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator extends State {

	private static final Orientation DEFAULT = Orientation.NATURAL;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;

	private Context context;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(final Context context) {
		listener = new UpdatingEventRegistrar<>(new UserErrorHandler(context));
		this.context = context;
	}

	@Override
	protected void start() throws EnvironmentException {
	}

	@Override
	protected void stop() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
	}

	public void setRecalibrating(boolean recalibrating) {
		this.recalibrating = recalibrating;
	}

	private boolean recalibrating;

	public void setDevice(Orientation newDevice) {

		if (recalibrating) {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0) {
				vibrate(85);
				user = user.plus(deviceChange);
			}
		}

		device = newDevice;
		reportUpdate();

	}

	private void vibrate(int durationMilliseconds) {
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(durationMilliseconds);
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
