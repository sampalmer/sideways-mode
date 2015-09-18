package samuelpalmer.sensorautorotation.notifications.service;

import android.graphics.Point;

import samuelpalmer.sensorautorotation.processes.service.screen.Screen;

public class NaturalOrientationDetector {

	private final Screen screen;

	public NaturalOrientationDetector(Screen screen) {
		this.screen = screen;
	}

	public boolean deviceIsNaturallyPortrait() {
		if (result == null)
			result = calculate();

		return result;
	}

	private Boolean result;

	private boolean calculate() {
		Point naturalResolution = screen.naturalResolution();
		return naturalResolution.x <= naturalResolution.y;
	}

}
