package samuelpalmer.sensorautorotation.unused;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.UserErrorHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ScreenTouchMonitor extends State {

	private View view;
	private WindowManager wm;
	private UpdatingEventRegistrar<Void> onTouch;

	public ScreenTouchMonitor(final Context context) {
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		view = new View(context);
		view.setAlpha(0);

		onTouch = new UpdatingEventRegistrar<>(new UserErrorHandler(context));

		view.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//Get actionMasked. Value 4 looks like it means TOUCH_OUTSIDE. Only covers "touch on" rather than movement or "touch off"
				v.performClick();
				onTouch.report(null);
				return false;
			}
		});
	}

	@Override
	protected void start() throws EnvironmentException {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				0, 0, 0, 0,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSPARENT);
		wm.addView(view, params);
	}

	@Override
	protected void stop() throws EnvironmentException {
		wm.removeView(view);
	}

	public EventRegistrar<Void> onTouch() {
		return onTouch;
	}
}
