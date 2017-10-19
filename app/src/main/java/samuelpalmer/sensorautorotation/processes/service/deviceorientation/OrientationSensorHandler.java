package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.NumberUtilities;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.errors.SensorError;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

import static android.content.Context.SENSOR_SERVICE;


public class OrientationSensorHandler extends State {
	
	private static final String TAG = OrientationSensorHandler.class.getSimpleName();
	
	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screen;

	private Integer lastRotation;
	private Integer oldBias;

	public static boolean canDetectOrientation() {
		SensorManager sensorManager = (SensorManager) CurrentProcess.context().getSystemService(SENSOR_SERVICE);
		ArrayList<Sensor> supportedSensors = WindowOrientationListener.getSupportedSensors(sensorManager);
		return !supportedSensors.isEmpty();
	}

	public OrientationSensorHandler(OrientationCalculator calculator, ScreenRotationMonitor screen) {
		this.calculator = calculator;
		this.screen = screen;

		if (!windowOrientationListener.canDetectOrientation())
			EnvironmentErrorManager.report(new SensorError(SensorError.Type.NOT_FOUND));
	}

	@Override
	protected void start() {
		lastRotation = null;
		updateRotationBias();
		windowOrientationListener.enable();

		calculator.onAnythingChanged.subscribe(screenUserOrAutoRotateChanged);
		screen.subscribe(screenOrientationChanged);
	}

	@Override
	protected void stop() {
		screen.unsubscribe(screenOrientationChanged);
		calculator.onAnythingChanged.unsubscribe(screenUserOrAutoRotateChanged);

		windowOrientationListener.disable();
	}

	private final WindowOrientationListener windowOrientationListener = new WindowOrientationListener(CurrentProcess.context(), new Handler(Looper.getMainLooper())) {
		@Override
		public void onProposedRotationChanged(int rotation) {
			if (!started()) {
				//This happens if you quickly repeatedly enable and disable the service on an emulator.
				//It might only affect older versions of Android (API 14-16)
				Log.w(TAG, "Got callback while stopped");
				return;
			}
			
			if (lastRotation == null || lastRotation != rotation) {
				lastRotation = rotation;
				
				Orientation orientation =
					rotation == -1
					?
					null
					:
					// Device orientations are anticlockwise, so we need to negate them for consistency.
					new AndroidSurfaceRotation(NumberUtilities.wrap(4 - rotation, 4)).orientation();
				
				Log.i(TAG, "Device orientation changed: " + orientation);
				calculator.setDevice(orientation);
			}
		}
	};
	
	private final EventHandler screenOrientationChanged = new EventHandler() {
		@Override
		public void update() {
			Log.i(TAG, "Screen orientation changed: " + screen.currentRotation());
			screenUserOrAutoRotateChanged.update();
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
		return screen.currentRotation().minus(calculator.user()).offset;
	}

}
