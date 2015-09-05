package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;

class LockedMode implements Mode {

	private final boolean portrait;

	public LockedMode(OrientationCalculator calculator, boolean deviceIsNaturallyPortrait) {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		portrait = inNaturalOrientation == deviceIsNaturallyPortrait;
	}

	@Override
	public int notificationIcon() {
		return portrait ? R.drawable.ic_portrait_lock : R.drawable.ic_landscape_lock;
	}

	@Override
	public int actionIcon() {
		return portrait ? R.drawable.ic_action_portrait_lock : R.drawable.ic_action_landscape_lock;
	}

	@Override
	public int autoRotationStateResourceId() {
		return portrait ? R.string.screen_rotation_disabled_portrait : R.string.screen_rotation_disabled_landscape;
	}

	@Override
	public int toggleToOtherModeAResourceId() {
		return R.string.action_enable_auto_rotation;
	}

	@Override
	public boolean equals(Mode other) {
		return
			LockedMode.class.equals(other.getClass())
			&&
			portrait == ((LockedMode)other).portrait;
	}

}
