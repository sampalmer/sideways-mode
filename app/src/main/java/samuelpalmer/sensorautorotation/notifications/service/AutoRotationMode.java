package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public int iconResourceId(boolean overridden) {
		return overridden ? R.drawable.ic_screen_rotation_enabled_overridden : R.drawable.ic_screen_rotation_enabled_normal;
	}

	@Override
	public int summaryResourceId() {
		return R.string.screen_rotation_enabled;
	}

	@Override
	public int actionDescriptionResourceId() {
		return R.string.disable_screen_rotation;
	}
}
