package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

		setContentView(R.layout.user_orientation);
		setRotation();

		RotationService.screenOrientationController.onOrientationChanged().subscribe(listener);
		RotationService.calculator.onChange().subscribe(listener);

		RotationService.calculator.setRecalibrating(true);
		registerReceiver(screenOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	private final BroadcastReceiver screenOff = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(screenOff);

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
