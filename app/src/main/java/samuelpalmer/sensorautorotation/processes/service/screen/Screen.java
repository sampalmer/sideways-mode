package samuelpalmer.sensorautorotation.processes.service.screen;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.view.Display;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.Orientation;

public class Screen {

	private final Display display;

	public Screen() {
		display = SystemServices.windowManager().getDefaultDisplay();
	}

	/**
	 * Expensive; prefer ScreenRotationMonitor.currentRotation()
	 */
	public Orientation realOrientation() {
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(display.getRotation());
		return rotation.orientation();
	}

	@SuppressLint("NewApi") /** Since {@link Display#getRealSize(Point)} is actually available as of API 14 */
	public Point screenResolution() {
		Point screenResolution = new Point();
		display.getRealSize(screenResolution);
		return screenResolution;
	}

}
