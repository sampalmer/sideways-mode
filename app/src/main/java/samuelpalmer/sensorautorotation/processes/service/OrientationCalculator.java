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
	private boolean waitingForSensor = true;

	public final UpdatingEventRegistrar onAnythingChanged;

	public OrientationCalculator(ScreenRotationMonitor screen, ApplicationState state) {
		this.screen = screen;
		this.state = state;
		onAnythingChanged = new UpdatingEventRegistrar();
	}

	public void reset() {
		device = DEFAULT;
		waitingForSensor = true;
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
		return offset.minus(baseScreenOrientation(!autoRotate));
	}

	private Rotation calculateOffset(boolean autoRotate, Orientation user) {
		return user.minus(baseScreenOrientation(!autoRotate));
	}

	private Rotation offset() {
		return state.offset.get();
	}

	public void offsetScreenBy(Rotation clockwiseRotations) {
		if (clockwiseRotations.isRevolution())
			//Nothing's changing. Don't want to proceed and get stuck in an infinite loop!
			return;

		Rotation newValue = offset();

		do
			newValue = newValue.plus(clockwiseRotations);
		while (calculateUser(autoRotate(), newValue).equals(Orientation.UPSIDE_DOWN));

		state.offset.set(newValue);
		reportChange();
	}

	public void setAutoRotate(boolean newValue) {
		/*
		 Since the current app/window might be overriding the current proposed orientation,
		 matching the actual screen orientation seems to provide a more consistent and intuitive user experience.

		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the user orientation) locks the user orientation derived from the screen.
		  */

		Rotation newOffset = screen.currentRotation().minus(baseScreenOrientation(newValue));
		newOffset = preventUpsideDownUser(newValue, newOffset);

		state.set(newValue, newOffset);
		reportChange();
	}

	private Rotation preventUpsideDownUser(boolean autoRotate, Rotation proposedOffset) {
		Orientation newUser = calculateUser(autoRotate, proposedOffset);

		if (newUser.equals(Orientation.UPSIDE_DOWN))
			return calculateOffset(autoRotate, Orientation.NATURAL);
		else
			return proposedOffset;
	}

	public void setDevice(Orientation newDevice) {
		waitingForSensor = false;

		if (newDevice != null)
			device = newDevice;

		reportChange();
	}

	private void reportChange() {
		onAnythingChanged.report();
	}

	public Orientation proposedScreenOrientation() {
		return baseScreenOrientation(autoRotate()).plus(offset());
	}

	/**
	 * The screen orientation before applying an offset.
	 * This seems to be a common factor in various calculations, but it was hard to come up with
	 * a good name for it.
	 */
	private Orientation baseScreenOrientation(boolean autoRotate) {
		return autoRotate ? device : Orientation.NATURAL;
	}

	public boolean isWaitingForSensor() {
		return waitingForSensor;
	}
}
