package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class ScreenOrientationEnforcer {

	final View view;
	final WindowManager windows;

	public ScreenOrientationEnforcer(Context owner) {
		windows = (WindowManager)owner.getSystemService(Context.WINDOW_SERVICE);
		view = new LinearLayout(owner);
		
		LayoutParams layout = generateLayout();
		windows.addView(view, layout);
		view.setVisibility(View.VISIBLE);
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutparams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
        layoutparams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;
		return layoutparams;
	}

	public void close() {
		windows.removeView(view);
	}

}
