package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.view.WindowManager;

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
