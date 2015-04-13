package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

import samuelpalmer.sensorautorotation.State;

public class ScreenOrientationEnforcer extends State {

	final View view;
	final WindowManager windows;

	public ScreenOrientationEnforcer(Context owner) {
		windows = (WindowManager) owner.getSystemService(Context.WINDOW_SERVICE);
		view = new LinearLayout(owner);
	}

	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSPARENT);
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
