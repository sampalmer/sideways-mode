package samuelpalmer.sensorautorotation.processes.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.Settings;
import samuelpalmer.sensorautorotation.values.Orientation;
import samuelpalmer.sensorautorotation.values.UserSide;

public class OrientationCalculator {
	
	private final Settings.Setting<UserSide> state;
	
	@NonNull
	private Orientation device;
	
	private boolean waitingForSensor = true;

	public final UpdatingEventRegistrar onAnythingChanged;

	public OrientationCalculator(ScreenRotationMonitor screen) {
		this.state = ServiceSettings.instance().persistentState();
		
		// This infers the device orientation from the screen orientation so we have something to start with.
		// It might be better to infer it from USER_ROTATION if system auto rotation was off.
		device = user().minus(screen.currentRotation());
		
		onAnythingChanged = new UpdatingEventRegistrar();
	}
	
	public @NonNull Orientation user() {
		return state.get().orientation;
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
		return user().minus(device);
	}
	
	public boolean isWaitingForSensor() {
		return waitingForSensor;
	}
	
	public UserSide side() {
		return state.get();
	}
	
	public void setSide(UserSide userSide) {
		state.set(userSide);
		reportChange();
	}
}
