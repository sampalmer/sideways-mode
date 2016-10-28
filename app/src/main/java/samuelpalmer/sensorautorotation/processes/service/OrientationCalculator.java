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
		state.offset.set(new Rotation(0));

		reportChange();
	}

	public boolean autoRotate() {
		return state.autoRotate.get();
	}

	public Orientation user() {
		return calculateUser(autoRotate(), offset());
	}

	private Orientation calculateUser(boolean autoRotate, Rotation offset) {
		return Orientation.NATURAL.plus(Orientation.NATURAL.minus(baseScreenOrientation(!effectivelyAutoRotate(autoRotate)).plus(offset)));
	}

	private Rotation calculateOffset(boolean autoRotate, Orientation user) {
		return user.minus(baseScreenOrientation(!effectivelyAutoRotate(autoRotate)));
	}

	private Rotation offset() {
		return state.offset.get();
	}

	public void offsetScreenBy(int clockwiseRotations) {
		Rotation newValue = offset();

		do
			newValue = new Rotation(newValue.clockwiseCount() - clockwiseRotations);
		while (calculateUser(autoRotate(), newValue).equals(Orientation.UPSIDE_DOWN));

		state.offset.set(newValue);
		reportChange();
	}

	public void setAutoRotate(boolean newValue) {
		/* Since the current app/window might be overriding the current proposed orientation.
		 Matching the actual screen orientation seems to provide a more consistent and intuitive newUser experience.
		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the newUser orientation) locks the newUser orientation derived from the screen. */

		Rotation newOffset = baseScreenOrientation(effectivelyAutoRotate(newValue)).minus(screen.currentRotation());
		newOffset = preventUpsideDownUser(newValue, newOffset);

		state.set(newValue, newOffset);
		reportChange();
	}

	public void setLockingOverride(boolean newValue) {
		if (this.lockingOverride != newValue) {
			this.lockingOverride = newValue;

			if (autoRotate())
				if (newValue)
					//Lock the device in its natural orientation
					state.offset.set(new Rotation(0));
				else {
					Rotation newOffset = device.minus(Orientation.NATURAL);
					newOffset = preventUpsideDownUser(autoRotate(), newOffset);

					state.offset.set(newOffset);
				}

			reportChange();
		}
	}

	private Rotation preventUpsideDownUser(boolean autoRotate, Rotation proposedOffset) {
		Orientation newUser = calculateUser(autoRotate, proposedOffset);

		if (newUser.equals(Orientation.UPSIDE_DOWN))
			return calculateOffset(autoRotate, Orientation.NATURAL);
		else
			return proposedOffset;
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
		return baseScreenOrientation(effectivelyAutoRotate(autoRotate())).minus(offset());
	}

	/**
	 * The screen orientation before applying an offset.
	 * This seems to be a common factor in various calculations, but it was hard to come up with
	 * a good name for it.
	 */
	private Orientation baseScreenOrientation(boolean effectivelyAutoRotate) {
		return effectivelyAutoRotate ? device : Orientation.NATURAL;
	}
}
