package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import samuelpalmer.sensorautorotation.errors.SensorError;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

import java.util.ArrayList;


public class OrientationSensorHandler extends State {
	
	private static final String TAG = OrientationSensorHandler.class.getSimpleName();
	
	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screen;
	private final WindowOrientationListener windowOrientationListener;
	
	private Integer lastRotation;
	private Integer oldBias;

	public static boolean canDetectOrientation(Context context) {
		SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		ArrayList<Sensor> supportedSensors = WindowOrientationListener.getSupportedSensors(sensorManager);
		return !supportedSensors.isEmpty();
	}

	public OrientationSensorHandler(Context context, OrientationCalculator calculator, ScreenRotationMonitor screen) {
		this.calculator = calculator;
		this.screen = screen;
		
		windowOrientationListener = new MyWindowOrientationListener(context);
		if (!windowOrientationListener.canDetectOrientation())
			EnvironmentErrorManager.report(context, new SensorError(context, SensorError.Type.NOT_FOUND));
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
	
	private final EventHandler screenOrientationChanged = new EventHandler() {
		@Override
		public void update() {
			Log.i(TAG, "Screen orientation changed: " + screen.currentRotation());
			screenUserOrAutoRotateChanged.update();
		}
	};
	
	private final EventHandler screenUserOrAutoRotateChanged = this::updateRotationBias;
	
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
	
	private class MyWindowOrientationListener extends WindowOrientationListener {
		private MyWindowOrientationListener(Context context) {
			super(context, new Handler(Looper.getMainLooper()));
		}
		
		@Override
		public void onProposedRotationChanged(int rotation) {
			if (!isStarted()) {
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
					AndroidSurfaceRotation.toOrientationAnticlockwise(rotation);
				
				Log.i(TAG, "Device orientation changed: " + orientation);
				calculator.setDevice(orientation);
			}
		}
	}
}
