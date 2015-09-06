package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;


public class OrientationSensorHandler extends State {

	private final OrientationCalculator calculator;

	private Integer lastRotation;
	public OrientationSensorHandler(OrientationCalculator calculator) {
		this.calculator = calculator;

		if (!windowOrientationListener.canDetectOrientation())
			//A user of an HTC One X reported that he/she got this error. Apparently system auto rotation also wasn't working, so it was probably a problem with the phone.
			EnvironmentError.report(R.string.missing_accelerometer_summary, R.string.missing_accelerometer_message, R.string.app_name);
	}

	@Override
	protected void start() {
		lastRotation = null;
		windowOrientationListener.setCurrentRotation(0);
		windowOrientationListener.enable();

		calculator.onAnythingChanged.subscribe(deviceRotationOrAutoRotationChanged);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(deviceRotationOrAutoRotationChanged);

		windowOrientationListener.disable();
	}

	private final WindowOrientationListener windowOrientationListener = new WindowOrientationListener(Process.context(), null) {
		@Override
		public void onProposedRotationChanged(int rotation) {
			if (lastRotation == null || lastRotation != rotation) {
				lastRotation = rotation;
				calculator.setDevice(new AndroidSurfaceRotation(rotation).orientation());
			}
		}
	};

	private final EventHandler deviceRotationOrAutoRotationChanged = new EventHandler() {
		@Override
		public void update() {
			updateRotationBias();
		}
	};

	private void updateRotationBias() {
		//Might help reduce incorrect device orientation predictions
		windowOrientationListener.setCurrentRotation(deviceRotationBias());
	}

	private int deviceRotationBias() {
		if (calculator.autoRotate() && lastRotation != null)
			return lastRotation;
		else
			return -1;
	}

}
