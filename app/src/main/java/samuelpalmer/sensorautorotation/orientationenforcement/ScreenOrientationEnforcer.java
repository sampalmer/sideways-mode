package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import samuelpalmer.sensorautorotation.State;

public class ScreenOrientationEnforcer extends State {

	private final View view;
	private final WindowManager windows;

	public ScreenOrientationEnforcer(Context context) {
		windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = new View(context);
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

	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams();

		//So it doesn't need a permission or activity
		layoutParams.type = LayoutParams.TYPE_TOAST;

		//Just in case the window type somehow doesn't enforce this
		layoutParams.flags =
				LayoutParams.FLAG_NOT_FOCUSABLE
						| LayoutParams.FLAG_NOT_TOUCHABLE;

		//Prevents breaking apps that detect overlying windows enabling
		//(eg UBank app, or enabling an accessibility service)
		layoutParams.width = 0;
		layoutParams.height = 0;

		//Try to make it completely invisible
		layoutParams.format = PixelFormat.TRANSPARENT;
		layoutParams.alpha = 0f;

		layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;

		return layoutParams;
	}

}
