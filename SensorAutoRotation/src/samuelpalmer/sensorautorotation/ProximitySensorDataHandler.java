package samuelpalmer.sensorautorotation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class ProximitySensorDataHandler implements SensorEventListener {
	
	private static final int THRESHOLD_IN_CENTIMETRES = 3;
	private OrientationCalculator calculator;

	public ProximitySensorDataHandler(OrientationCalculator calculator) {
		this.calculator = calculator;
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
		float coveredThreshold = Math.min(THRESHOLD_IN_CENTIMETRES, event.sensor.getMaximumRange());
		boolean covered = distanceInCentimetres <= coveredThreshold;
		calculator.setCovered(covered);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
