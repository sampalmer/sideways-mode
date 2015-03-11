package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final ApplicationSettings settings;
	private final Screen screen;
	private final ApplicationState state;

	private Orientation device = DEFAULT;
	private boolean deviceSpecified;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(ApplicationSettings settings, Screen screen, ApplicationState state) {
		this.settings = settings;
		this.screen = screen;
		this.state = state;
		listener = new UpdatingEventRegistrar<>();
	}

	public void reset() {
		device = DEFAULT;
		Orientation newUser = DEFAULT;

		ApplicationSettings.ScreenOnBehaviour screenOnBehaviour = settings.screenOnBehaviour().get();
		if (screenOnBehaviour == ApplicationSettings.ScreenOnBehaviour.LOCK_SCREEN_ROTATION)
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
			/* Since the current app/window might be overriding the current proposed orientation.
			 Matching the actual screen orientation seems to provide a more consistent and intuitive user experience;
			 locking screen orientation locks the actual orientation rather than the proposed one. */
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
