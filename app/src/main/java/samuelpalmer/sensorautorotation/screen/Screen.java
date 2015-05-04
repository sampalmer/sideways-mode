package samuelpalmer.sensorautorotation.screen;

import android.content.Context;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.Orientation;

public class Screen {

	private final Context context;

	public Screen(Context context) {

		this.context = context;
	}

	public Orientation realOrientation() {
		WindowManager windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(windows.getDefaultDisplay().getRotation());
		return rotation.orientation();
	}

}
