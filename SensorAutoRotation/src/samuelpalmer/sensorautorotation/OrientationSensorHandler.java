package samuelpalmer.sensorautorotation;

import android.content.Context;
import com.android.internal.policy.impl.WindowOrientationListener;

public class OrientationSensorHandler extends WindowOrientationListener {

	private OrientationCalculator calculator;

	public OrientationSensorHandler(Context context, OrientationCalculator calculator) {
		super(context, null);
		this.calculator = calculator;
	}

	@Override
	public void onProposedRotationChanged(int rotation) {
		Orientation orientation = Orientation.fromAndroidSurfaceRotation(rotation);
		calculator.setDevice(orientation);
	}
	
}
