package samuelpalmer.sensorautorotation;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PhysicalOrientationTracker {
	final SensorManager sensorManager;
	final SensorEventListener listener;
	final Sensor[] sensors;
	
	public PhysicalOrientationTracker(SensorManager sensorManager, SensorEventListener listener) throws EnvironmentException {
		this.sensorManager = sensorManager;
		this.listener = listener;
		this.sensors = new Sensor[] {
			getSensor(Sensor.TYPE_PROXIMITY, "proximity")
		};
		
		registerSensors();
	}

	private Sensor getSensor(int type, String description) throws EnvironmentException {
		Sensor result = sensorManager.getDefaultSensor(type);
		if (result == null)
			throw new EnvironmentException("No " + description + " sensor");
		
		return result;
	}
	
	private void registerSensors() throws EnvironmentException {
		for (Sensor sensor : sensors)
			registerSensor(sensor);
	}
	
	private void registerSensor(Sensor sensor) throws EnvironmentException {
		boolean registered = sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + sensor);
	}
	
	public void close() {
		sensorManager.unregisterListener(listener);
	}
}
