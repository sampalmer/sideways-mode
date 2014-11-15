package samuelpalmer.sensorautorotation;

import android.content.Context;
import com.android.internal.policy.impl.WindowOrientationListener;

public class OrientationSensorHandler extends WindowOrientationListener {

	private OrientationCalculator calculator;
	private Context context;

	public OrientationSensorHandler(Context context, OrientationCalculator calculator) throws EnvironmentException {
		super(context, null);
		if (!canDetectOrientation())
			throw new EnvironmentException("Couldn't find an accelerometer");
		
		this.context = context;
		this.calculator = calculator;
	}

	@Override
	public void onProposedRotationChanged(int rotation) {
		Orientation orientation = Orientation.fromAndroidSurfaceRotation(rotation);
		
		try {
			calculator.setDevice(orientation);
		} catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
	}
	
}
