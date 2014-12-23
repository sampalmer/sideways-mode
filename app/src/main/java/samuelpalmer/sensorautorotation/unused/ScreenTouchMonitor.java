package samuelpalmer.sensorautorotation.unused;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class ScreenTouchMonitor {

	private View view;
	private WindowManager wm;

	public ScreenTouchMonitor(final Context context) {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				0, 0, 0, 0,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSPARENT);
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = new View(context);
		view.setAlpha(0);
		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Get actionMasked. Value 4 looks like it means TOUCH_OUTSIDE. Only covers "touch on" rather than movement or "touch off"
				v.performClick();
				return false;
			}
		});
		wm.addView(view, params);
	}

	public void close() {
		if (view != null) {
			wm.removeView(view);
			view = null;
		}
	}

}
