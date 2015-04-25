package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import samuelpalmer.sensorautorotation.State;

public class ScreenOrientationEnforcer extends State {

	final View view;
	final WindowManager windows;

	public ScreenOrientationEnforcer(Context owner) {
		windows = (WindowManager) owner.getSystemService(Context.WINDOW_SERVICE);
		view = new View(owner);
	}

	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams();
		layoutParams.width = 0;
		layoutParams.height = 0;
		layoutParams.type = LayoutParams.TYPE_TOAST;
		layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		layoutParams.format = PixelFormat.TRANSPARENT;
		layoutParams.alpha = 0f;
		layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
		return layoutParams;
	}

	@Override
	protected void start() {
		LayoutParams layout = generateLayout();
		windows.addView(view, layout);
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void stop() {
		windows.removeView(view);
	}
}
