package samuelpalmer.sensorautorotation.processes.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.notifications.service.DeviceStatus;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class OrientationCalculator {

	private final ScreenRotationMonitor screen;
	private final ApplicationState state;
	
	/**
	 * This is initially <c>null</c> to prevent the following situation:
	 * - App is off
	 * - Device is in auto rotate mode
	 * - Screen is in landscape
	 * - Device is lying flat
	 * - User turns app on
	 * - App thinks user is upright
	 * - User presses the rotate left or right button
	 * - App thinks user is sideways
	 * - User lifts up device
	 * - Screen rotates to face sideways
	 */
	@Nullable
	private Orientation device;
	
	private boolean waitingForSensor = true;

	public final UpdatingEventRegistrar onAnythingChanged;

	public OrientationCalculator(ScreenRotationMonitor screen, ApplicationState state) {
		this.screen = screen;
		this.state = state;
		onAnythingChanged = new UpdatingEventRegistrar();
	}

	public void reset() {
		device = Orientation.NATURAL;
		waitingForSensor = true;
		state.offset.set(new Rotation(0));

		reportChange();
	}

	public boolean autoRotate() {
		return state.autoRotate.get();
	}

	public @Nullable Orientation user() {
		return calculateUser(autoRotate(), offset());
	}

	private @Nullable Orientation calculateUser(boolean autoRotate, @NonNull Rotation offset) {
		Orientation base = baseScreenOrientation(!autoRotate);
		return base == null ? null : offset.minus(base);
	}

	private @NonNull Rotation calculateOffset(boolean autoRotate, @NonNull Orientation user) {
		Orientation base = baseScreenOrientation(!autoRotate);
		return base == null ? new Rotation(0) : user.minus(base);
	}

	private @NonNull Rotation offset() {
		return state.offset.get();
	}

	public void offsetScreenBy(Rotation clockwiseRotations) {
		if (clockwiseRotations.isRevolution())
			//Nothing's changing. Don't want to proceed and get stuck in an infinite loop!
			return;
		
		Rotation newValue = offset();
		
		do
			newValue = newValue.plus(clockwiseRotations);
		while (Orientation.UPSIDE_DOWN.equals(calculateUser(autoRotate(), newValue)));
		
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
		
		Orientation base = baseScreenOrientation(newValue);
		Rotation newOffset = base == null ? new Rotation(0) : screen.currentRotation().minus(base);
		newOffset = preventUpsideDownUser(newValue, newOffset);

		state.set(newValue, newOffset);
		reportChange();
	}

	private @NonNull Rotation preventUpsideDownUser(boolean autoRotate, @NonNull Rotation proposedOffset) {
		Orientation newUser = calculateUser(autoRotate, proposedOffset);

		if (Orientation.UPSIDE_DOWN.equals(newUser))
			return calculateOffset(autoRotate, Orientation.NATURAL);
		else
			return proposedOffset;
	}

	public void setDevice(@Nullable Orientation newDevice) {
		waitingForSensor = false;
		
		if (newDevice != null)
			device = newDevice;
		
		reportChange();
	}

	private void reportChange() {
		onAnythingChanged.report();
	}

	public @Nullable Orientation proposedScreenOrientation() {
		Orientation base = baseScreenOrientation(autoRotate());
		return base == null ? null : base.plus(offset());
	}

	/**
	 * The screen orientation before applying an offset.
	 * This seems to be a common factor in various calculations, but it was hard to come up with
	 * a good name for it.
	 */
	private @Nullable Orientation baseScreenOrientation(boolean autoRotate) {
		return autoRotate ? device : Orientation.NATURAL;
	}

	public DeviceStatus deviceStatus() {
		if (waitingForSensor)
			return DeviceStatus.WAITING_FOR_SENSOR;
		else if (proposedScreenOrientation() == null)
			return DeviceStatus.WAITING_FOR_ORIENTATION;
		else
			return DeviceStatus.OK;
	}
}
