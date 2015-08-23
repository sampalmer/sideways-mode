package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public int icon(boolean overridden) {
		return overridden ? R.drawable.ic_action_rotation_overridden : R.drawable.ic_action_rotation;
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
