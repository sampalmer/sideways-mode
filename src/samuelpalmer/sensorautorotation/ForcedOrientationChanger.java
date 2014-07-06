package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class ForcedOrientationChanger implements OrientationChanger {
	final View view;
	final WindowManager windows;
	private int lastOrientation;

	public ForcedOrientationChanger(Context owner, int initialOrientation) {
		windows = (WindowManager)owner.getSystemService(Context.WINDOW_SERVICE);
		view = new LinearLayout(owner);
		lastOrientation = initialOrientation;
		
		LayoutParams layout = generateLayout(initialOrientation);
		windows.addView(view, layout);
		view.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void set(int orientation) {
		if (orientation != lastOrientation) {
			LayoutParams newLayout = generateLayout(orientation);
			windows.updateViewLayout(view, newLayout);
			lastOrientation = orientation;
		}
	}
	
	private LayoutParams generateLayout(int orientation) {
		LayoutParams layoutparams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.RGBA_8888);
        layoutparams.screenOrientation = orientation;
		return layoutparams;
	}

	@Override
	public void close() {
		windows.removeView(view);
	}
}
