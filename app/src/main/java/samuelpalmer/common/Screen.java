package samuelpalmer.common;

import android.view.Display;
import android.view.WindowManager;

public class Screen {

	private final Display display;

	public Screen(WindowManager windowManager) {
		display = windowManager.getDefaultDisplay();
	}

	/**
	 * Expensive; prefer ScreenRotationMonitor.currentRotation()
	 */
	public Orientation realOrientation() {
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(display.getRotation());
		return rotation.orientation();
	}
	
}
