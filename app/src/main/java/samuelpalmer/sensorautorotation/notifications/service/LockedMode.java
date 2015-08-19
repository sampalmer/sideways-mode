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
	public String iconSvgFileName(boolean overridden) {
		return
				portrait
						?
						(overridden ? "portrait_overridden.svg" : "portrait_lock.svg")
						:
						(overridden ? "landscape_overridden.svg" : "landscape_lock.svg");

	}

	@Override
	public int iconResourceId() {
		return
				portrait ? R.drawable.ic_portrait_lock : R.drawable.ic_landscape_lock;
	}

	@Override
	public int autoRotationStateResourceId() {
		return portrait ? R.string.screen_rotation_disabled_portrait : R.string.screen_rotation_disabled_landscape;
	}

}
