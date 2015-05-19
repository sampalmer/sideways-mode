package samuelpalmer.sensorautorotation.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.Orientation;

public class Screen {

	private final WindowManager windows;

	public Screen(Context context) {
		windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public Orientation realOrientation() {
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(windows.getDefaultDisplay().getRotation());
		return rotation.orientation();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public boolean deviceIsNaturallyPortrait() {
		Display display = windows.getDefaultDisplay();
		Point screenResolution = new Point();

		//Getting the screen dimensions and rotation should be as close together as possible
		//since it's possible for them to be retrieved out of sync
		if (Build.VERSION.SDK_INT >= 14)
			display.getRealSize(screenResolution);
		else
			display.getSize(screenResolution);
		int rotation = display.getRotation();

		Orientation orientation = new AndroidSurfaceRotation(rotation).orientation();

		boolean portrait = screenResolution.x < screenResolution.y;
		return portrait == orientation.isNaturalOrUpsideDown();
	}

}
