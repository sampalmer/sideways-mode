package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import samuelpalmer.sensorautorotation.State;

/**
 * Requires the {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW} permission.
 */
public class ScreenOrientationEnforcer extends State {

	private static final String TAG = ScreenOrientationEnforcer.class.getSimpleName();

	private final View view;
	private final WindowManager windows;
	private final LayoutParams layout;
	private boolean isEnforced;

	public ScreenOrientationEnforcer(Context context) {
		windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = new View(context);
		layout = generateLayout();
	}

	@SuppressLint("NewApi")
	public static boolean hasPermissionToStart(Context context) {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
	}

	@Override
	public boolean started() {
		return super.started();
	}

	@Override
	protected void start() {
		setEnforcedUnconditionally(false);
		windows.addView(view, layout);
		Log.i(TAG, "Added screen orientation enforcement overlay");
	}

	@Override
	protected void stop() {
		windows.removeView(view);
		Log.i(TAG, "Removed screen orientation enforcement overlay");
	}

	private static LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams();

		//This seems like the most relevant window type
		//Note that TYPE_TOAST can't be used to bypass the permission requirement since it still needs the permission in MIUI V8
		if (Build.VERSION.SDK_INT >= 26)
			//noinspection WrongConstant
			layoutParams.type = LayoutParams.TYPE_APPLICATION_OVERLAY;
		else
			//noinspection deprecation
			layoutParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;

		//Just in case the window type somehow doesn't enforce this
		layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;

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
		verifyStarted();

		if (newEnforced != isEnforced) {
			setEnforcedUnconditionally(newEnforced);
			Log.i(TAG, "Set screen orientation enforcement: " + newEnforced);
		}
	}

	private void setEnforcedUnconditionally(boolean newEnforced) {
		isEnforced = newEnforced;
		int visibility = newEnforced ? View.VISIBLE : View.INVISIBLE;
		view.setVisibility(visibility);
	}

}
