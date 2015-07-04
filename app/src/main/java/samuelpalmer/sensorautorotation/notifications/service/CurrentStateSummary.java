package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.processes.service.Orientation;

public class CurrentStateSummary {

	private final Mode mode;
	private final Orientation user;

	public CurrentStateSummary(Mode mode, Orientation user) {
		this.mode = mode;
		this.user = user;
	}

	public CharSequence buildTitle() {
		return StringResource.format(R.string.notification_summary, mode.autoRotationStateResourceId(), userStateResourceId());
	}

	private int userStateResourceId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

}
