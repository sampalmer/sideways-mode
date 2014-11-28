package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.android.WindowOrientationListener;
import android.content.Context;


public class OrientationSensorHandler extends ExternalResource {

	private WindowOrientationListener listener;

	public OrientationSensorHandler(final Context context, final OrientationCalculator calculator) throws EnvironmentException {
		listener = new WindowOrientationListener(context, null) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				Orientation orientation = new AndroidSurfaceRotation(rotation).orientation();
				calculator.setDevice(orientation);
			}
		};
		
		if (!listener.canDetectOrientation())
			throw new EnvironmentException("Couldn't find an accelerometer");
	}

	@Override
	protected void start() {
		listener.enable();
	}

	@Override
	protected void stop() {
		listener.disable();
	}
	
}
