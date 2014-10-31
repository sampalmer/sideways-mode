package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class PhysicalOrientationTracker {
	final SensorManager sensorManager;
	final SensorEventListener listener;
	final Sensor proximitySensor;
	
	boolean enabled;
	
	public PhysicalOrientationTracker(Context context, OrientationCalculator calculator) {
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		listener = new RotatorProximitySensorDataHandler(calculator);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			throw new EnvironmentException("No proximity sensor");
	}

	public void enable() {
		if (enabled)
			throw new RuntimeException("Already enabled");
		
		boolean registered = sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + proximitySensor);
		
		enabled = true;
	}
	
	public void disable() {
		if (!enabled)
			throw new RuntimeException("Not enabled");
		
		sensorManager.unregisterListener(listener);
		
		enabled = false;
	}
}
