package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.Orientation;
import samuelpalmer.sensorautorotation.values.UserSide;

public class OrientationCalculator {
	
	private final SharedPreferences prefs;
	
	@NonNull
	private Orientation device;
	
	private boolean waitingForSensor = true;

	public final UpdatingEventRegistrar onAnythingChanged;

	public OrientationCalculator(Context context, ScreenRotationMonitor screen) {
		this.prefs = ServiceSettings.getSharedPreferences(context);
		
		// This infers the device orientation from the screen orientation so we have something to start with.
		// It might be better to infer it from USER_ROTATION if system auto rotation was off.
		device = user().minus(screen.currentRotation());
		
		onAnythingChanged = new UpdatingEventRegistrar();
	}
	
	public @NonNull Orientation user() {
		return side().orientation;
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
		return proposedScreenOrientationForUser(user());
	}
	
	public @NonNull Orientation proposedScreenOrientationForUser(Orientation user) {
		return user.minus(device);
	}
	
	public void setSide(UserSide userSide) {
		prefs
			.edit()
			.putString(ServiceSettings.KEY_PERSISTENT_STATE, userSide.name())
			.apply();
		
		reportChange();
	}
	
	public ApplicationStatus.RunningState runningState() {
		return new ApplicationStatus.RunningState(side(), waitingForSensor);
	}
	
	private UserSide side() {
		String name = prefs.getString(ServiceSettings.KEY_PERSISTENT_STATE, null);
		if (name == null)
			throw new RuntimeException("Service has no state; don't know user side.");
		
		return UserSide.valueOf(name);
	}
	
}
