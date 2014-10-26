package samuelpalmer.sensorautorotation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class RotatorProximitySensorDataHandler implements SensorEventListener {
	
	private AutoRotator rotator;

	public RotatorProximitySensorDataHandler(AutoRotator x) {
		this.rotator = x;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		
		if (sensorType == Sensor.TYPE_PROXIMITY)
			updateProximity(event);
		else
			throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
	}

	private void updateProximity(SensorEvent event) {
		float distanceInCentimetres = event.values[0];
		float coveredThreshold = Math.min(3, event.sensor.getMaximumRange());
		boolean covered = distanceInCentimetres <= coveredThreshold;
		rotator.setProximitySensorCovered(covered);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
