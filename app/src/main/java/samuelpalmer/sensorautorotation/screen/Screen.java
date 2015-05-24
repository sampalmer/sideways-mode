package samuelpalmer.sensorautorotation.screen;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.SystemServices;

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

		if (Build.VERSION.SDK_INT >= 14)
			display.getRealSize(screenResolution);
		else
			display.getSize(screenResolution);

		return screenResolution;
	}

}
