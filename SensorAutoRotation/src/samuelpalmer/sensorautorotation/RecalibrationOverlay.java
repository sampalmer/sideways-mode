package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class RecalibrationOverlay implements ProximitySensorHandler.Listener {
	
	private WindowManager windowManager;
	private LinearLayout overlay;
	private Context context;

	public RecalibrationOverlay(Context context) {
		this.context = context;
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
	}

	public void stop() {
		ensureNotPresent();
	}
	
	@Override
	public void update(boolean covered) {
		if (covered)
			ensurePresent();
		else
			ensureNotPresent();
	}

	private void ensurePresent() {
		if (overlay == null) {
			overlay = new LinearLayout(context);
			overlay.setBackgroundColor(Color.BLACK);
			overlay.setVisibility(View.VISIBLE);
			windowManager.addView(overlay, generateLayout());
		}
	}

	private void ensureNotPresent() {
		if (overlay != null) {
			windowManager.removeView(overlay);
			overlay = null;
		}
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.3f;
		return layoutParams;
	}

}
