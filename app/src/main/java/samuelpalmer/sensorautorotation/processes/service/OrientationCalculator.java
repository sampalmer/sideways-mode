package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final ScreenRotationMonitor screen;
	private final ApplicationState state;

	private Orientation device = DEFAULT;

	public final UpdatingEventRegistrar onAnythingChanged;
	private boolean lockingOverride;

	public OrientationCalculator(ScreenRotationMonitor screen, ApplicationState state) {
		this.screen = screen;
		this.state = state;
		onAnythingChanged = new UpdatingEventRegistrar();
	}

	public void reset() {
		device = DEFAULT;
		state.user.set(DEFAULT);

		reportChange();
	}

	public boolean autoRotate() {
		return state.autoRotate.get();
	}

	public Orientation user() {
		return state.user.get();
	}

	public void offsetUser(int clockwiseRotations) {
		Orientation newUser = user().plus(new Rotation(-clockwiseRotations));
		state.user.set(newUser);
		reportChange();
	}

	public void setAutoRotate(boolean newValue) {
		/* Since the current app/window might be overriding the current proposed orientation.
		 Matching the actual screen orientation seems to provide a more consistent and intuitive user experience.
		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the user orientation) locks the user orientation derived from the screen. */
		state.set(newValue, inferredUser());
		reportChange();
	}

	public void setLockingOverride(boolean lockingOverride) {
		this.lockingOverride = lockingOverride;
		reportChange();
	}

	private Orientation inferredUser() {
		return Orientation.NATURAL.plus(device.minus(screen.currentRotation()));
	}

	public void setDevice(Orientation newDevice) {

		if (!effectivelyAutoRotate()) {
			Rotation deviceChange = newDevice.minus(device);
			state.user.set(state.user.get().plus(deviceChange));
		}

		device = newDevice;

		reportChange();

	}

	public boolean effectivelyAutoRotate() {
		return autoRotate() && !lockingOverride;
	}

	private void reportChange() {
		onAnythingChanged.report();
	}

	public Orientation proposedScreenOrientation() {
		return Orientation.NATURAL.plus(device.minus(state.user.get()));
	}
}
