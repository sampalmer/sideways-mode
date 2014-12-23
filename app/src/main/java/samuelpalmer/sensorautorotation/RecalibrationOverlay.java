package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.github.rongi.rotate_layout.layout.RotateLayout;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class RecalibrationOverlay extends State {

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

	@SuppressLint("InflateParams")
	@Override
	protected void start() throws EnvironmentException {
		layout = layoutInflater.inflate(R.layout.user_orientation, null);
		layout.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				finish();
				return true;
			}
		});

		layout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				finish();
			}
		});

		setRotation();
		windowManager.addView(layout, generateLayoutParams());

		screenOrientationController.onOrientationChanged().subscribe(listener);
		calculator.setRecalibrating(true);
		calculator.onChange().subscribe(listener);
	}

	private void finish() {
		try {
			ensureStopped();
		} catch (EnvironmentException e) {
			RotationService.errorHandler.handle(e);
		}
	}

	@Override
	protected void stop() throws EnvironmentException {
		calculator.onChange().unsubscribe(listener);
		calculator.setRecalibrating(false);
		screenOrientationController.onOrientationChanged().unsubscribe(listener);

		windowManager.removeView(layout);
	}

	private void setRotation() {
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
		LayoutParams layoutParams = new LayoutParams(
				LayoutParams.TYPE_SYSTEM_ERROR,
				LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);
		layoutParams.alpha = 0.4f;
		return layoutParams;
	}

	private final EventHandler<Void> listener = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			RecalibrationOverlay.this.setRotation();
		}
	};
}
