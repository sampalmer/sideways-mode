package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final ScreenRotationMonitor screen;
	private final ApplicationState state;

	private Orientation device = DEFAULT;

	private final UpdatingEventRegistrar listener;

	public OrientationCalculator(ScreenRotationMonitor screen, ApplicationState state) {
		this.screen = screen;
		this.state = state;
		listener = new UpdatingEventRegistrar();
	}

	public void reset() {
		device = DEFAULT;
		Orientation newUser = DEFAULT;

		state.user(newUser);

		reportUpdate();
	}

	public boolean autoRotate() {
		return state.autoRotate();
	}

	public Orientation user() {
		return state.user();
	}

	public void offsetUser(int clockwiseRotations) {
		Orientation newUser = user().plus(new Rotation(-clockwiseRotations));
		state.user(newUser);
		reportUpdate();
	}

	public void setAutoRotate(boolean newValue) {
		/* Since the current app/window might be overriding the current proposed orientation.
		 Matching the actual screen orientation seems to provide a more consistent and intuitive user experience.
		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the user orientation) locks the user orientation derived from the screen. */
		state.set(newValue, inferredUser());
		reportUpdate();
	}

	private Orientation inferredUser() {
		return Orientation.NATURAL.plus(device.minus(screen.currentRotation()));
	}

	public void setDevice(Orientation newDevice) {

		if (!autoRotate()) {
			Rotation deviceChange = newDevice.minus(device);
			state.user(state.user().plus(deviceChange));
		}

		device = newDevice;

		reportUpdate();

	}

	private void reportUpdate() {
		listener.report();
	}

	public EventRegistrar onChange() {
		return listener;
	}

	public Orientation proposedScreenOrientation() {
		return device.plus(userOffset());
	}

	private Rotation userOffset() {
		return Orientation.NATURAL.minus(state.user());
	}
}
