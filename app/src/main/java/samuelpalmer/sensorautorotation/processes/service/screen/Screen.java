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

	public Orientation realOrientation() {
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(display.getRotation());
		return rotation.orientation();
	}

	public boolean deviceIsNaturallyPortrait() {
		//Getting the screen dimensions and rotation should be as close together as possible
		//since it's possible for them to be retrieved out of sync
		Point screenResolution = screenResolution();
		Orientation orientation = realOrientation();

		boolean portrait = screenResolution.x < screenResolution.y;
		return portrait == orientation.isNaturalOrUpsideDown();
	}

	@SuppressLint("NewApi") /** Since {@link Display#getRealSize(Point)} is actually available as of API 14 */
	private Point screenResolution() {
		Point screenResolution = new Point();
		display.getRealSize(screenResolution);
		return screenResolution;
	}

}
