package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ProximitySensorHandler extends State {

	private static final float THRESHOLD_IN_CENTIMETRES = .3f;
	
	private final SensorManager sensorManager;
	
	private final UpdatingEventRegistrar<ProximitySensorCoveredState> onChange;
	public EventRegistrar<ProximitySensorCoveredState> onChange() {
		return onChange;
	}
	
	public ProximitySensorHandler(Context context) {
		onChange = new UpdatingEventRegistrar<>(new UserErrorHandler(context));
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
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
			boolean covered = distanceInCentimetres <= coveredThreshold;
			onChange.report(new ProximitySensorCoveredState(covered));
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};
	
}
