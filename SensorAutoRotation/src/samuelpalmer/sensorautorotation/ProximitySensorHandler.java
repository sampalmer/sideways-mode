package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

public class ProximitySensorHandler extends ExternalResource {

	private static final float THRESHOLD_IN_CENTIMETRES = .3f;
	
	private final SensorManager sensorManager;
	private final Handler handler;
	private final Runnable r = new Runnable() {
		@Override
		public void run() {
			onChange.report(new ProximitySensorCoveredState(covered));
		}
	};
	
	private final UpdatingEventRegistrar<ProximitySensorCoveredState> onChange;
	public EventRegistrar<ProximitySensorCoveredState> onChange() {
		return onChange;
	}
	
	public ProximitySensorHandler(Context context) {
		onChange = new UpdatingEventRegistrar<ProximitySensorCoveredState>(new UserErrorHandler(context));
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		handler = new Handler();
	}
	
	protected void start() throws EnvironmentException {
		Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			throw new EnvironmentException("No proximity sensor");
		
		boolean registered = sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + proximitySensor);
	}
	
	protected void stop() {
		dequeueUpdates();
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
					handler.postDelayed(r, 400);
				else {
					dequeueUpdates();
					r.run();
				}
			}
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	
	private void dequeueUpdates() {
		handler.removeCallbacks(r);
	}
	
	private boolean covered;
	
}
