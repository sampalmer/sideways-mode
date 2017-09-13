package samuelpalmer.sensorautorotation.processes.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class OrientationCalculator {
	
	private final ApplicationState state;
	
	@NonNull
	private Orientation device;
	
	private boolean waitingForSensor = true;

	public final UpdatingEventRegistrar onAnythingChanged;

	public OrientationCalculator(ScreenRotationMonitor screen, ApplicationState state) {
		this.state = state;
		
		// This infers the device orientation from the screen orientation so we have something to start with.
		// It might be better to infer it from USER_ROTATION if system auto rotation was off.
		device = screen.currentRotation().minus(offset());
		
		onAnythingChanged = new UpdatingEventRegistrar();
	}

	public void reset() {
		device = Orientation.NATURAL;
		waitingForSensor = true;
		state.offset.set(new Rotation(0));

		reportChange();
	}
	
	public @NonNull Orientation user() {
		return calculateUser(offset());
	}

	private @NonNull Orientation calculateUser( @NonNull Rotation offset) {
		Orientation base = baseScreenOrientation(false);
		return offset.minus(base);
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
		while (calculateUser(newValue).equals(Orientation.UPSIDE_DOWN));
		
		state.offset.set(newValue);
		reportChange();
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

	public @NonNull Orientation proposedScreenOrientation() {
		Orientation base = baseScreenOrientation(true);
		return base.plus(offset());
	}

	/**
	 * The screen orientation before applying an offset.
	 * This seems to be a common factor in various calculations, but it was hard to come up with
	 * a good name for it.
	 */
	private @NonNull Orientation baseScreenOrientation(boolean autoRotate) {
		return autoRotate ? device : Orientation.NATURAL;
	}

	public boolean isWaitingForSensor() {
		return waitingForSensor;
	}
}
