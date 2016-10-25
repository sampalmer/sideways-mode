package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.common.IntegerUtilities;
import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class UserOrientation {

	private final Orientation user;
	private final int rotations;

	public UserOrientation(Orientation user, ScreenRotationMonitor screenRotationMonitor, Orientation proposed) {
		this.user = user;
		rotations = IntegerUtilities.wrap(screenRotationMonitor.currentRotation().minus(proposed).clockwiseCount(), 4);
	}

	public int descriptionId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

	public int iconId() {
		switch (rotations) {
			case 0: return R.drawable.user_upright;
			case 1: return R.drawable.user_left_side;
			case 2: return R.drawable.user_upside_down;
			case 3: return R.drawable.user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + rotations);
		}
	}

	public boolean equals(UserOrientation other) {
		return
			user.equals(other.user)
			&&
			rotations == other.rotations;
	}

}
