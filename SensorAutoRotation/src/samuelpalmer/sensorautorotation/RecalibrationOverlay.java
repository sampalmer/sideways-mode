package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.Screen.RealOrientationListener;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.caverock.androidsvg.SVGImageView;
import com.github.rongi.rotate_layout.layout.RotateLayout;

public class RecalibrationOverlay implements ProximitySensorHandler.Listener {
	
	private WindowManager windowManager;
	private RotateLayout layout;
	private Context context;
	private Screen screen;
	private SVGImageView image;
	
	public RecalibrationOverlay(Screen screen, Context context) {
		this.screen = screen;
		this.context = context;
		
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
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
			layout = new RotateLayout(context);
			
			image = new SVGImageView(context);
			image.setImageAsset("portrait.svg");
			adjustRotation();
			
			layout.addView(image);
			
			layout.setBackgroundColor(Color.BLACK);
			layout.setVisibility(View.VISIBLE);
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

	private void adjustRotation() {
		Orientation proposed = screen.proposedOrientation();
		Orientation actual = screen.realOrientation();
		
		RotateLayout.LayoutParams params = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		params.angle = 90 * actual.minus(proposed).clockwiseCount();
		image.setLayoutParams(params);
	}
	
	private final RealOrientationListener listener = new RealOrientationListener() {
		@Override
		public void possiblyChanged() {
			if (layout != null)
				adjustRotation();
		}
	};
}
