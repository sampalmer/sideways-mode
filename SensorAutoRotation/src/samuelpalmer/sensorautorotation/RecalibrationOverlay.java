package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.OrientationCalculator.UserListener;
import samuelpalmer.sensorautorotation.Screen.RealOrientationListener;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.github.rongi.rotate_layout.layout.RotateLayout;

public class RecalibrationOverlay implements ProximitySensorHandler.Listener {
	
	private WindowManager windowManager;
	private View layout;
	private Screen screen;
	private LayoutInflater layoutInflater;
	private OrientationCalculator calculator;
	
	public RecalibrationOverlay(Screen screen, Context context, OrientationCalculator calculator) {
		this.screen = screen;
		this.calculator = calculator;
		
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = layoutInflater.inflate(R.layout.user_orientation, null);
		ensureNotPresent();
		windowManager.addView(layout, generateLayout());
		
		screen.listen(screenListener);
		calculator.listen(userListener);
	}

	public void stop() {
		screen.stopListening();
		windowManager.removeView(layout);
	}
	
	@Override
	public void update(boolean covered) {
		if (covered)
			ensurePresent();
		else
			ensureNotPresent();
	}

	private void ensurePresent() {
        layout.setVisibility(View.VISIBLE);
	}

	private void update() {
		Orientation proposedScreenOrientation = calculator.getDevice().plus(Orientation.NATURAL.minus(calculator.getUser()));
		Orientation actual = screen.realOrientation();
		RotateLayout.LayoutParams subcontainerParams = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		subcontainerParams.angle = 90 * actual.minus(proposedScreenOrientation).clockwiseCount();
		
		View subcontainer = layout.findViewById(R.id.overlay_subcontainer);
		subcontainer.setLayoutParams(subcontainerParams);
	}

	private void ensureNotPresent() {
        layout.setVisibility(View.GONE);
	}
	
	private LayoutParams generateLayout() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.4f;
		return layoutParams;
	}

	private final RealOrientationListener screenListener = new RealOrientationListener() {
		@Override
		public void possiblyChanged() {
			updateOverlay();
		}
	};
	
	private UserListener userListener = new UserListener() {
		
		@Override
		public void update() {
			updateOverlay();
		}
	};
	
	private void updateOverlay() {
		update();
	}
	
}
