package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.screen.Screen;

class LockedMode implements Mode {

	private final OrientationCalculator calculator;
	private final Screen screen;

	public LockedMode(OrientationCalculator calculator, Screen screen) {
		this.calculator = calculator;
		this.screen = screen;
	}

	@Override
	public int iconResourceId(boolean overridden) {
		return
				isPortrait()
						?
						(overridden ? R.drawable.ic_screen_rotation_locked_portrait_overridden : R.drawable.ic_screen_rotation_locked_portrait_normal)
						:
						(overridden ? R.drawable.ic_screen_rotation_locked_landscape_overridden : R.drawable.ic_screen_rotation_locked_landscape_normal);
	}

	private boolean isPortrait() {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		return inNaturalOrientation == screen.deviceIsNaturallyPortrait();
	}

	@Override
	public int summaryResourceId() {
		return R.string.screen_rotation_disabled;
	}

	@Override
	public int actionDescriptionResourceId() {
		return R.string.enable_screen_rotation;
	}
}
