package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.IntegerUtilities;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class UserOrientation {

	private final Orientation user;
	private final Orientation proposed;
	private final Orientation screen;

	public UserOrientation(Orientation user, ScreenRotationMonitor screenRotationMonitor, Orientation proposed) {
		this.user = user;
		this.screen = screenRotationMonitor.currentRotation();
		this.proposed = proposed;
	}

	public int descriptionId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

	public int iconId() {
		int rotations = IntegerUtilities.wrap(screen.minus(proposed).clockwiseCount(), 4);

		switch (rotations) {
			case 0: return R.drawable.ic_user_upright;
			case 1: return R.drawable.ic_user_left_side;
			case 2: return R.drawable.ic_user_upside_down;
			case 3: return R.drawable.ic_user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + rotations);
		}
	}

}
