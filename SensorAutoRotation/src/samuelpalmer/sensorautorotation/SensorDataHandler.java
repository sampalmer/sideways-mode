package samuelpalmer.sensorautorotation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

//TODO: Split this into 2 classes

public class SensorDataHandler implements SensorEventListener {
	
	private final LowPassFilter gravity;
	private X x;

	public SensorDataHandler(X x) {
		this.x = x;
		this.gravity = new LowPassFilter();
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		
		switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				updateOrientation(event);
				break;
			case Sensor.TYPE_PROXIMITY:
				updateProximity(event);
				break;
			default:
				throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
		}
	}

	private void updateProximity(SensorEvent event) {
		float distanceInCentimetres = event.values[0];
		float coveredThreshold = Math.min(3, event.sensor.getMaximumRange());
		boolean covered = distanceInCentimetres <= coveredThreshold;
		x.setProximitySensorCovered(covered);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	private void updateOrientation(SensorEvent event) {
		float[] components = gravity.process(event);
		
		DeviceAcceleration acceleration = new DeviceAcceleration(components[0], components[1]);
		if (acceleration.hasOrientation())
			this.x.setDeviceOrientation(acceleration.orientation());
	}
}
