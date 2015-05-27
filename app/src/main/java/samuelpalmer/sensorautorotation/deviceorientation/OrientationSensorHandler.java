package samuelpalmer.sensorautorotation.deviceorientation;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.ThisApplication;


public class OrientationSensorHandler extends State {

	private final OrientationCalculator calculator;

	private Integer lastRotation;

	public OrientationSensorHandler(OrientationCalculator calculator) {
		this.calculator = calculator;

		if (!listener.canDetectOrientation())
			//A user of an HTC One X reported that he/she got this error. Apparently system auto rotation also wasn't working, so it was probably a problem with the phone.
			EnvironmentError.report(R.string.missing_accelerometer_message);
	}

	@Override
	protected void start() {
		lastRotation = null;
		listener.setCurrentRotation(-1);
		listener.enable();
	}

	@Override
	protected void stop() {
		listener.disable();
	}

	private final WindowOrientationListener listener = new WindowOrientationListener(ThisApplication.context(), null) {
		@Override
		public void onProposedRotationChanged(int rotation) {
			listener.setCurrentRotation(rotation); //Might help reduce incorrect device orientation predictions

			if (lastRotation == null || lastRotation != rotation) {
				calculator.setDevice(new AndroidSurfaceRotation(rotation).orientation());
				lastRotation = rotation;
			}
		}
	};

}
