package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.Screen.RealOrientationListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.caverock.androidsvg.SVGImageView;
import com.github.rongi.rotate_layout.layout.RotateLayout;

public class RecalibrationOverlay implements ProximitySensorHandler.Listener {
	
	private WindowManager windowManager;
	private View layout;
	private Screen screen;
	private LayoutInflater layoutInflater;
	
	public RecalibrationOverlay(Screen screen, Context context) {
		this.screen = screen;
		
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		screen.listen(listener);
	}

	public void stop() {
		screen.stopListening();
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
		if (layout == null) {
	        layout = layoutInflater.inflate(R.layout.user_orientation, null);
	        
	        Orientation proposed = screen.proposedOrientation();
			Orientation actual = screen.realOrientation();
			RotateLayout.LayoutParams subcontainerParams = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			subcontainerParams.angle = 90 * actual.minus(proposed).clockwiseCount();
			
			View subcontainer = layout.findViewById(R.id.overlay_subcontainer);
	        subcontainer.setLayoutParams(subcontainerParams);
			
			windowManager.addView(layout, generateLayout());
		}
	}

	private void ensureNotPresent() {
		if (layout != null) {
			windowManager.removeView(layout);
			layout = null;
		}
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.3f;
		return layoutParams;
	}

	private final RealOrientationListener listener = new RealOrientationListener() {
		@Override
		public void possiblyChanged() {
			if (layout != null) {
				ensureNotPresent();
				ensurePresent();
			}
		}
	};
}
