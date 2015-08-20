package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.Orientation;

public class UserOrientation {

	private final Orientation user;

	public UserOrientation(Orientation user) {
		this.user = user;
	}

	public int descriptionId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

	public int iconId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.drawable.ic_user_upright;
			case 1: return R.drawable.ic_user_left_side;
			case 2: return R.drawable.ic_user_upside_down;
			case 3: return R.drawable.ic_user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + user.getClockwiseRotationsFromNaturalOrientation());
		}
	}

}
