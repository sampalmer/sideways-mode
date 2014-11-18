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

	public RecalibrationOverlay(Context context) {
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		overlay = new LinearLayout(context);
		overlay.setBackgroundColor(Color.BLACK);
		windowManager.addView(overlay, generateLayout());
	}

	public void stop() {
		windowManager.removeView(overlay);
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.3f;
		return layoutParams;
	}
	
	@Override
	public void update(boolean covered) {
		if (covered)
			overlay.setVisibility(View.VISIBLE);
		else
			overlay.setVisibility(View.GONE);
	}

}
