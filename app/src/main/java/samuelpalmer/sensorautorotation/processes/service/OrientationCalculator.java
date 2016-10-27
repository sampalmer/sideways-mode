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

	//TODO: WARNING: This seems to be backwards; left side and right side user orientations are swapped!
	private Orientation calculateUser(boolean autoRotate, Orientation offset) {
		//TODO: Consolidate this logic with the proposed screen orientation logic

		return (effectivelyAutoRotate(autoRotate) ? Orientation.NATURAL : device).plus(offset.minus(Orientation.NATURAL));
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
		boolean newEffectivelyAutoRotate = effectivelyAutoRotate(newValue);
		Orientation newOffset = Orientation.NATURAL.plus((newEffectivelyAutoRotate ? device : Orientation.NATURAL).minus(screen.currentRotation()));
		Orientation newUser = calculateUser(newValue, newOffset);

		//TODO: Add fallback in case the new newUser orientation will be upside-down
//		if (newUser.equals(Orientation.UPSIDE_DOWN))
//			if (newEffectivelyAutoRotate)
//				newOffset = Orientation.NATURAL;
//			else
//				newOffset =

		state.set(newValue, newOffset);
		reportChange();
	}

	public void setLockingOverride(boolean lockingOverride) {
		//TODO: Fix this; it broke when the meaning of the user state variable changed
		if (this.lockingOverride != lockingOverride) {
			if (autoRotate())
				if (lockingOverride) {
					device = Orientation.NATURAL;
				} else if (user().equals(Orientation.UPSIDE_DOWN)) {
					state.offset.set(Orientation.NATURAL);
				}

			this.lockingOverride = lockingOverride;
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
