package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.OrientationCalculator.Listener;
import samuelpalmer.sensorautorotation.Screen.RealOrientationListener;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.github.rongi.rotate_layout.layout.RotateLayout;

public class RecalibrationOverlay extends ExternalResource {
	
	private WindowManager windowManager;
	private LayoutInflater layoutInflater;
	private View layout;
	private Screen screen;
	private OrientationCalculator calculator;

	public RecalibrationOverlay(Context context, Screen screen, OrientationCalculator calculator) {
		this.screen = screen;
		this.calculator = calculator;
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	protected void start() throws EnvironmentException {
		layout = layoutInflater.inflate(R.layout.user_orientation, null);
		update();
		windowManager.addView(layout, generateLayoutParams());
		
		screen.listen(screenListener);
		calculator.listen(listener);
	}

	@Override
	protected void stop() throws EnvironmentException {
		calculator.stopListening();
		screen.stopListening();
		
		windowManager.removeView(layout);
	}

	private void update() {
		Orientation proposedScreenOrientation = calculator.getDevice().plus(Orientation.NATURAL.minus(calculator.getUser()));
		Orientation actual = screen.realOrientation();
		RotateLayout.LayoutParams subcontainerParams = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		subcontainerParams.angle = 90 * actual.minus(proposedScreenOrientation).clockwiseCount();
		
		View subcontainer = layout.findViewById(R.id.overlay_subcontainer);
		subcontainer.setLayoutParams(subcontainerParams);
	}
	
	private LayoutParams generateLayoutParams() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.4f;
		return layoutParams;
	}
	
	
	private final RealOrientationListener screenListener = new RealOrientationListener() {
		@Override
		public void possiblyChanged() {
			update();
		}
	};
	
	private final Listener listener = new Listener() {
		
		@Override
		public void update() {
			update();
		}
	};
	
}
