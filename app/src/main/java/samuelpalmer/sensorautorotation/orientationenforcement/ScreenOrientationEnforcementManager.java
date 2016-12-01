package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import samuelpalmer.sensorautorotation.State;

public class ScreenOrientationEnforcementManager extends State {

	private static final String TAG = ScreenOrientationEnforcementManager.class.getSimpleName();

	private final View view;
	private final WindowManager windows;
	private LayoutParams layout;
	private boolean isEnforced;

	public ScreenOrientationEnforcementManager(Context context) {
		windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = new View(context);
		setEnforcedUnconditionally(false);
		layout = generateLayout();
	}

	@Override
	protected void start() {
		windows.addView(view, layout);
	}

	@Override
	protected void stop() {
		windows.removeView(view);
	}

	private static LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams();

		//So it doesn't need a permission or activity
		layoutParams.type = LayoutParams.TYPE_TOAST;

		//Just in case the window type somehow doesn't enforce this
		layoutParams.flags =
				LayoutParams.FLAG_NOT_FOCUSABLE
						| LayoutParams.FLAG_NOT_TOUCHABLE;

		//Prevents breaking apps that detect overlying windows
		//(eg UBank app, or enabling an accessibility service)
		layoutParams.width = 0;
		layoutParams.height = 0;

		//Try to make it completely invisible
		layoutParams.format = PixelFormat.TRANSPARENT;
		layoutParams.alpha = 0f;

		//Match the system's "user orientation" (which this app uses to specify screen orientation)
		layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;

		return layoutParams;
	}

	/**
	 * This seems to be the quickest way of toggling enforcement. Changing the view visibility is
	 * slower, and removing and adding the view is even slower again.
	 *
	 * There are a few ways of toggling screen orientation enforcement:
	 * 1. Fastest: Toggle its {@link LayoutParams#screenOrientation} value. This causes the Android 6 "Screen Overlay Detected" problem even when enforcement is disabled.
	 * 2. Medium: Toggle the view's visibility.
	 * 3. Slowest: Add/remove the view from the window manager.
	 *
	 * Picking the middle option since it's the fastest one that doesn't have undesired side effects.
	 */
	public void setEnforced(boolean newEnforced) {
		if (newEnforced != isEnforced)
			setEnforcedUnconditionally(newEnforced);
	}

	private void setEnforcedUnconditionally(boolean newEnforced) {
		isEnforced = newEnforced;
		int visibility = newEnforced ? View.VISIBLE : View.INVISIBLE;
		view.setVisibility(visibility);
		Log.i(TAG, "Set screen orientation enforcement: " + newEnforced);
	}

}
