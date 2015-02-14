package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final Context context;
	private final Screen screen;
	private final ApplicationState state;

	private Orientation device = DEFAULT;
	private boolean deviceSpecified;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(Context context, Screen screen, ApplicationState state) {
		this.context = context;
		this.screen = screen;
		this.state = state;
		listener = new UpdatingEventRegistrar<>();
	}

	public void reset() {
		device = DEFAULT;
		Orientation newUser = DEFAULT;

		Settings.ScreenOnBehaviour screenOnBehaviour = new Settings(context).screenOnBehaviour().get();
		if (screenOnBehaviour == Settings.ScreenOnBehaviour.LOCK_SCREEN_ROTATION)
			state.set(false, newUser);
		else
			state.user(newUser);

		reportUpdate();
	}

	public boolean isRecalibrating() {
		return !state.autoRotate();
	}

	public void setRecalibrating(boolean newValue) {
		if (newValue) {
			inferUserFromScreen();
			reportUpdate();
		}

		state.autoRotate(!newValue);
	}

	private void inferUserFromScreen() {
		state.user(inferredUser());
	}

	private Orientation inferredUser() {
		return Orientation.NATURAL.plus(device.minus(screen.realOrientation()));
	}

	public void setDevice(Orientation newDevice) {

		if (isRecalibrating()) {
			Rotation deviceChange = newDevice.minus(device);
			state.user(state.user().plus(deviceChange));
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
		return Orientation.NATURAL.minus(state.user());
	}

}
