package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public String iconSvgFileName(boolean overridden) {
		return overridden ? "rotation_overridden.svg" : "rotation.svg";
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
