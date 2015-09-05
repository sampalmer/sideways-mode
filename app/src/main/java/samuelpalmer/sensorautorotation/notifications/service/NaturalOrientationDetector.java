package samuelpalmer.sensorautorotation.notifications.service;

import android.graphics.Point;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class NaturalOrientationDetector {

	private final Screen screen;
	private final ScreenRotationMonitor screenRotationMonitor;

	public NaturalOrientationDetector(Screen screen, ScreenRotationMonitor screenRotationMonitor) {
		this.screen = screen;
		this.screenRotationMonitor = screenRotationMonitor;
	}

	public boolean deviceIsNaturallyPortrait() {
		if (result == null)
			result = calculate();

		return result;
	}

	private Boolean result;

	private boolean calculate() {
		//Getting the screen dimensions and rotation should be as close together as possible
		//since it's possible for them to be retrieved out of sync
		Point screenResolution = screen.screenResolution();
		Orientation orientation = screenRotationMonitor.currentRotation();

		boolean portrait = screenResolution.x < screenResolution.y;
		return portrait == orientation.isNaturalOrUpsideDown();
	}

}
