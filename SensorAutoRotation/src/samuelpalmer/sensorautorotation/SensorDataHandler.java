package samuelpalmer.sensorautorotation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

//TODO: Split this into 2 classes

public class SensorDataHandler implements SensorEventListener {
	
	private X x;

	public SensorDataHandler(X x) {
		this.x = x;
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
		x.setProximitySensorCovered(covered);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
