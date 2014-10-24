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
		final int threshold = 1;
		
		//TODO: Organise and test all this code
		float[] components = gravity.process(event);
		float x = components[0];
		float y = components[1];
		
		float xMagnitude = Math.abs(x);
		float yMagnitude = Math.abs(y);
		
		float differenceInMagnitudes = Math.abs(xMagnitude - yMagnitude);
		if (differenceInMagnitudes > threshold) {
			boolean isMoreVertical = yMagnitude > xMagnitude;
			
			int clockwiseRotationsFromNaturalOrientation;
			
			if (isMoreVertical && yMagnitude > threshold)
				if (y > 0)
					clockwiseRotationsFromNaturalOrientation = 0;
				else
					clockwiseRotationsFromNaturalOrientation = 2;
			else if (xMagnitude > threshold)
				if (x > 0)
					clockwiseRotationsFromNaturalOrientation = 1;
				else
					clockwiseRotationsFromNaturalOrientation = 3;
			else
				return;
			
			Orientation orientation = new Orientation(clockwiseRotationsFromNaturalOrientation);
			this.x.setDeviceOrientation(orientation);
		}
	}
}
