package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;

class LockedMode implements Mode {

	private final boolean portrait;

	public LockedMode(OrientationCalculator calculator, Screen screen) {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		portrait = inNaturalOrientation == screen.deviceIsNaturallyPortrait();
	}

	@Override
	public int icon(boolean overridden) {
		return
				portrait
						?
						(overridden ? R.drawable.ic_action_portrait_overridden : R.drawable.ic_action_portrait_lock)
						:
						(overridden ? R.drawable.ic_action_landscape_overridden : R.drawable.ic_action_landscape_lock);

	}

	@Override
	public int autoRotationStateResourceId() {
		return portrait ? R.string.screen_rotation_disabled_portrait : R.string.screen_rotation_disabled_landscape;
	}

	@Override
	public int toggleToOtherModeAResourceId() {
		return R.string.action_enable_auto_rotation;
	}

}
