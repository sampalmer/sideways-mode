package samuelpalmer.sensorautorotation.notification.service;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public int iconResourceId() {
		return R.drawable.ic_screen_rotation_enabled;
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
