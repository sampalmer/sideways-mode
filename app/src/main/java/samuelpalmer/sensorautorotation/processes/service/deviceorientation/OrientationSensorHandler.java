package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;


public class OrientationSensorHandler extends State {

	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screen;

	private Integer lastRotation;
	private Integer oldBias;

	public OrientationSensorHandler(OrientationCalculator calculator, ScreenRotationMonitor screen) {
		this.calculator = calculator;
		this.screen = screen;

		if (!windowOrientationListener.canDetectOrientation())
			WindowOrientationListener.reportSensorError(R.string.environment_error_sensor_missing_message);
	}

	@Override
	protected void start() {
		lastRotation = null;
		updateRotationBias();
		windowOrientationListener.enable();

		calculator.onAnythingChanged.subscribe(screenUserOrAutoRotateChanged);
		screen.subscribe(screenUserOrAutoRotateChanged);
	}

	@Override
	protected void stop() {
		screen.unsubscribe(screenUserOrAutoRotateChanged);
		calculator.onAnythingChanged.unsubscribe(screenUserOrAutoRotateChanged);

		windowOrientationListener.disable();
	}

	private final WindowOrientationListener windowOrientationListener = new WindowOrientationListener(Process.context(), new Handler(Looper.getMainLooper())) {
		@Override
		public void onProposedRotationChanged(int rotation) {
			if (!started()) {
				//This happens if you quickly repeatedly enable and disable the service on an emulator.
				//It might only affect older versions of Android (API 14-16)
				Log.w(OrientationSensorHandler.class.getSimpleName(), "Got callback while stopped");
				return;
			}

			if (lastRotation == null || lastRotation != rotation) {
				lastRotation = rotation;
				calculator.setDevice(new AndroidSurfaceRotation(rotation).orientation());
			}
		}
	};

	private final EventHandler screenUserOrAutoRotateChanged = new EventHandler() {
		@Override
		public void update() {
			updateRotationBias();
		}
	};

	private void updateRotationBias() {
		int newBias = deviceRotationBias();
		if (oldBias == null || newBias != oldBias) {
			windowOrientationListener.setCurrentRotation(newBias);
			oldBias = newBias;
		}
	}

	private int deviceRotationBias() {
		if (calculator.effectivelyAutoRotate(calculator.autoRotate()))
			return Orientation.NATURAL.plus(screen.currentRotation().minus(calculator.user())).getClockwiseRotationsFromNaturalOrientation();
		else
			return -1;
	}

}
