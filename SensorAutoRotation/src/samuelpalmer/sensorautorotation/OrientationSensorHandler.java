package samuelpalmer.sensorautorotation;

import android.content.Context;


public class OrientationSensorHandler extends ExternalResource {

	private WindowOrientationListener listener;

	public OrientationSensorHandler(final Context context, final OrientationCalculator calculator) throws EnvironmentException {
		listener = new WindowOrientationListener(context, null) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				Orientation orientation = Orientation.fromAndroidSurfaceRotation(rotation);
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
