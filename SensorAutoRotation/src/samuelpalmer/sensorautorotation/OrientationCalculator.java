package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class OrientationCalculator {
	
	private static final int THRESHOLD_IN_CENTIMETRES = 3;
	
	private Orientation device;
	private Screen screen;
	private SensorManager sensorManager;
	private boolean covered;
	private SensorEventListener listener;
	
	public OrientationCalculator(Context context, Screen screen) throws EnvironmentException {
		this.screen = screen;
		
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		listener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				int sensorType = event.sensor.getType();
				
				if (sensorType != Sensor.TYPE_PROXIMITY)
					throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
				
				float distanceInCentimetres = event.values[0];
				float coveredThreshold = Math.min(THRESHOLD_IN_CENTIMETRES, event.sensor.getMaximumRange());
				covered = distanceInCentimetres <= coveredThreshold;
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		};
		
		Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			throw new EnvironmentException("No proximity sensor");
		
		boolean registered = sensorManager.registerListener(this.listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + proximitySensor);
	}
	
	public void setDevice(Orientation newDevice) throws EnvironmentException {
		
		if (device != null) {
			Rotation deviceChange = newDevice.minus(device);
			Orientation currentScreenOrientation = screen.orientation();
			Orientation proposedScreenOrientation = currentScreenOrientation.plus(deviceChange);
			if (!covered)
				screen.propose(proposedScreenOrientation);
		}
		
		device = newDevice;
	}

	public void stop() {
		sensorManager.unregisterListener(listener);
	}
	
}
