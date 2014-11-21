package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

public class ProximitySensorHandler {

	private static final float THRESHOLD_IN_CENTIMETRES = .3f;
	
	private final SensorManager sensorManager;
	private final Listener[] listeners;
	private boolean stopped;

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
		stopped = true;
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
			
			if (newCovered != covered) {
				covered = newCovered;
				if (covered)
					new Handler().postDelayed(new Runnable() {
						
						@Override
						public void run() {
							if (!stopped)
								updateListeners();
						}
					}, 400);
				else
					updateListeners();
			}
		}

		private void updateListeners() {
			for (int i = 0; i < listeners.length; ++i)
				listeners[i].update(covered);
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	
	private boolean covered;
	
	public interface Listener {
		public void update(boolean covered);
	}
	
}
