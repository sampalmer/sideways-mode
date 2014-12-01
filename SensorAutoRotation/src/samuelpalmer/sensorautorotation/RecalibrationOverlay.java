package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import android.annotation.SuppressLint;
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
	private ScreenOrientationController screenOrientationController;
	private OrientationCalculator calculator;

	public RecalibrationOverlay(Context context, ScreenOrientationController screenOrientationController, OrientationCalculator calculator) {
		this.screenOrientationController = screenOrientationController;
		this.calculator = calculator;
		windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@SuppressLint("InflateParams") @Override
	protected void start() throws EnvironmentException {
		layout = layoutInflater.inflate(R.layout.user_orientation, null);
		update();
		windowManager.addView(layout, generateLayoutParams());
		
		screenOrientationController.onOrientationChanged().subscribe(screenOrientationListener);
		calculator.onChange().subscribe(proposedOrientationListener);
	}

	@Override
	protected void stop() throws EnvironmentException {
		calculator.onChange().unsubscribe(proposedOrientationListener);
		screenOrientationController.onOrientationChanged().unsubscribe(screenOrientationListener);
		
		windowManager.removeView(layout);
	}

	private void update() {
		View subcontainer = layout.findViewById(R.id.overlay_subcontainer);
		subcontainer.setLayoutParams(calculateRotation());
	}

	private RotateLayout.LayoutParams calculateRotation() {
		Orientation actual = screenOrientationController.realOrientation();
		RotateLayout.LayoutParams subcontainerParams = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		subcontainerParams.angle = 90 * actual.minus(calculator.proposedScreenOrientation()).clockwiseCount();
		
		return subcontainerParams;
	}
	
	private LayoutParams generateLayoutParams() {
		LayoutParams layoutParams = new LayoutParams(LayoutParams.TYPE_SYSTEM_OVERLAY, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.4f;
		return layoutParams;
	}
	
	private final EventHandler<Void> screenOrientationListener = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			RecalibrationOverlay.this.update();
		}
	};
	
	private final EventHandler<Void> proposedOrientationListener = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			RecalibrationOverlay.this.update();
		}
	};
}
