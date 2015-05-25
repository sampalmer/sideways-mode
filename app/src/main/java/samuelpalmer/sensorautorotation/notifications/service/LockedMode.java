package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.screen.Screen;

class LockedMode implements Mode {

	private final boolean portrait;

	public LockedMode(OrientationCalculator calculator, Screen screen) {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		portrait = inNaturalOrientation == screen.deviceIsNaturallyPortrait();
	}

	@Override
	public int iconResourceId(boolean overridden) {
		return
				portrait
						?
						(overridden ? R.drawable.ic_screen_rotation_locked_portrait_overridden : R.drawable.ic_screen_rotation_locked_portrait_normal)
						:
						(overridden ? R.drawable.ic_screen_rotation_locked_landscape_overridden : R.drawable.ic_screen_rotation_locked_landscape_normal);
	}

	@Override
	public int autoRotationStateResourceId() {
		return portrait ? R.string.screen_rotation_disabled_portrait : R.string.screen_rotation_disabled_landscape;
	}

	@Override
	public int actionDescriptionResourceId() {
		return R.string.enable_screen_rotation;
	}
}
