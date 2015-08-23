package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public int notificationIcon() {
		return R.drawable.ic_rotation;
	}

	@Override
	public int actionIcon() {
		return R.drawable.ic_action_rotation;
	}

	@Override
	public int autoRotationStateResourceId() {
		return R.string.screen_rotation_enabled;
	}

	@Override
	public int toggleToOtherModeAResourceId() {
		return R.string.action_disable_auto_rotation;
	}

}
