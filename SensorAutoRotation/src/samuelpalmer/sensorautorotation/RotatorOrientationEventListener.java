package samuelpalmer.sensorautorotation;

import android.content.Context;
import com.android.internal.policy.impl.WindowOrientationListener;

public class RotatorOrientationEventListener extends WindowOrientationListener {

	private OrientationCalculator calculator;

	public RotatorOrientationEventListener(Context context, OrientationCalculator calculator) {
		super(context, null);
		this.calculator = calculator;
	}

	@Override
	public void onProposedRotationChanged(int rotation) {
		Orientation orientation = Orientation.fromAndroidSurfaceRotation(rotation);
		calculator.setDevice(orientation);
	}
	
}
