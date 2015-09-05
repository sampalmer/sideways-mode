package samuelpalmer.sensorautorotation.notifications.service;

import android.graphics.Point;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

class LockedMode implements Mode {

	private final boolean portrait;

	public LockedMode(OrientationCalculator calculator, Screen screen, ScreenRotationMonitor screenRotationMonitor) {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		portrait = inNaturalOrientation == deviceIsNaturallyPortrait(screen, screenRotationMonitor);
	}

	public static boolean deviceIsNaturallyPortrait(Screen screen, ScreenRotationMonitor screenRotationMonitor) {
		//Getting the screen dimensions and rotation should be as close together as possible
		//since it's possible for them to be retrieved out of sync
		Point screenResolution = screen.screenResolution();
		Orientation orientation = screenRotationMonitor.currentRotation();

		boolean portrait = screenResolution.x < screenResolution.y;
		return portrait == orientation.isNaturalOrUpsideDown();
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
