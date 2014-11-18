package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ProximitySensorHandler {

	private static final int THRESHOLD_IN_CENTIMETRES = 3;
	
	private final SensorManager sensorManager;
	private final Listener[] listeners;
	private Boolean covered;

	public ProximitySensorHandler(Context context, Listener[] listeners) throws EnvironmentException {
		this.listeners = listeners;
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		
		Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			throw new EnvironmentException("No proximity sensor");
		
		boolean registered = sensorManager.registerListener(this.listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + proximitySensor);
	}
	
	public void stop() {
		sensorManager.unregisterListener(listener);
	}
	
	private final SensorEventListener listener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			int sensorType = event.sensor.getType();
			
			if (sensorType != Sensor.TYPE_PROXIMITY)
				throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
			
			float distanceInCentimetres = event.values[0];
			float coveredThreshold = Math.min(THRESHOLD_IN_CENTIMETRES, event.sensor.getMaximumRange());
			boolean newCovered = distanceInCentimetres <= coveredThreshold;
			if (covered == null || newCovered != covered) {
				covered = newCovered;
				for (int i = 0; i < listeners.length; ++i)
					listeners[i].stateChanged(newCovered);
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	
	public interface Listener {
		public void stateChanged(boolean covered);
	}
	
}
