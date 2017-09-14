package samuelpalmer.sensorautorotation.processes.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.UserSide;
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
	
	public @NonNull Orientation user() {
		return offset().minus(Orientation.NATURAL);
	}
	
	private @NonNull Rotation offset() {
		return state.userSide.get().offset();
	}

	public void changeSide() {
		UserSide newSide;
		
		UserSide currentSide = state.userSide.get();
		switch (currentSide) {
			case LEFT:
				newSide = UserSide.RIGHT;
				break;
			case RIGHT:
				newSide = UserSide.LEFT;
				break;
			default:
				throw new RuntimeException("Can't determine opposite side of user side '" + currentSide.toString() + "'.");
		}
		
		state.userSide.set(newSide);
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
		return device.plus(offset());
	}
	
	public boolean isWaitingForSensor() {
		return waitingForSensor;
	}
	
	public UserSide side() {
		return state.userSide.get();
	}
}
