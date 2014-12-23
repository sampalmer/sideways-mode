package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.github.rongi.rotate_layout.layout.RotateLayout;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class RecalibrationActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View layout = getLayoutInflater().inflate(R.layout.user_orientation, null);
        LayoutParams params = new LayoutParams(LayoutParams.TYPE_SYSTEM_ALERT, LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.alpha = 0.4f;
        setContentView(layout, params);

		setRotation();

        RotationService.screenOrientationController.onOrientationChanged().subscribe(listener);
        RotationService.calculator.onChange().subscribe(listener);

        RotationService.calculator.setRecalibrating(true);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        RotationService.calculator.setRecalibrating(false);

        RotationService.calculator.onChange().unsubscribe(listener);
        RotationService.screenOrientationController.onOrientationChanged().unsubscribe(listener);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus)
            finish();
    }

	private void setRotation() {
		View subcontainer = findViewById(R.id.overlay_subcontainer);
		subcontainer.setLayoutParams(calculateRotation());
	}

	private RotateLayout.LayoutParams calculateRotation() {
		Orientation actual = RotationService.screenOrientationController.realOrientation();
		RotateLayout.LayoutParams subcontainerParams = new RotateLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		subcontainerParams.angle = 90 * actual.minus(RotationService.calculator.proposedScreenOrientation()).clockwiseCount();
		
		return subcontainerParams;
	}

    private final EventHandler<Void> listener = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			RecalibrationActivity.this.setRotation();
		}
	};
}
