package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
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
		state.offset.set(DEFAULT);

		reportChange();
	}

	public boolean autoRotate() {
		return state.autoRotate.get();
	}

	public Orientation user() {
		return calculateUser(autoRotate(), offset());
	}

	private Orientation calculateUser(boolean autoRotate, Orientation offset) {
		//TODO: Consolidate this logic with the proposed screen orientation logic

		return (effectivelyAutoRotate(autoRotate) ? Orientation.NATURAL : device).plus(offset.minus(Orientation.NATURAL));
	}

	private Orientation calculateOffset(boolean autoRotate, Orientation user) {
		//TODO: Consolidate this logic with the proposed screen orientation logic
		return Orientation.NATURAL.plus(user.minus(effectivelyAutoRotate(autoRotate) ? Orientation.NATURAL : device));
	}

	private Orientation offset() {
		return state.offset.get();
	}

	//TODO: Rename this
	public void offsetUser(int clockwiseRotations) {
		Orientation newValue = offset();

		do
			newValue = newValue.plus(new Rotation(-clockwiseRotations));
		while (calculateUser(autoRotate(), newValue).equals(Orientation.UPSIDE_DOWN));

		state.offset.set(newValue);
		reportChange();
	}

	public void setAutoRotate(boolean newValue) {
		/* Since the current app/window might be overriding the current proposed orientation.
		 Matching the actual screen orientation seems to provide a more consistent and intuitive newUser experience.
		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the newUser orientation) locks the newUser orientation derived from the screen. */

		//TODO: Consolidate this logic with the proposed screen orientation logic
		Orientation newOffset = Orientation.NATURAL.plus((effectivelyAutoRotate(newValue) ? device : Orientation.NATURAL).minus(screen.currentRotation()));

		Orientation newUser = calculateUser(newValue, newOffset);
		if (newUser.equals(Orientation.UPSIDE_DOWN))
			newOffset = calculateOffset(newValue, Orientation.NATURAL);

		state.set(newValue, newOffset);
		reportChange();
	}

	public void setLockingOverride(boolean lockingOverride) {
		if (this.lockingOverride != lockingOverride) {
			this.lockingOverride = lockingOverride;

			if (autoRotate()) {
				if (lockingOverride || user().equals(Orientation.UPSIDE_DOWN))
					state.offset.set(calculateOffset(true, Orientation.NATURAL));

				if (lockingOverride)
					device = Orientation.NATURAL;
			}

			reportChange();
		}
	}

	public void setDevice(Orientation newDevice) {
		device = newDevice;
		reportChange();
	}

	public boolean effectivelyAutoRotate(boolean autoRotate) {
		return autoRotate && !lockingOverride;
	}

	private void reportChange() {
		onAnythingChanged.report();
	}

	public Orientation proposedScreenOrientation() {
		return Orientation.NATURAL.plus((effectivelyAutoRotate(autoRotate()) ? device : Orientation.NATURAL).minus(offset()));
	}
}
