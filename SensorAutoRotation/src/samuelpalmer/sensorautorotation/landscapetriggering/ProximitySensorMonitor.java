package samuelpalmer.sensorautorotation.landscapetriggering;

import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.UserErrorHandler;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ProximitySensorMonitor extends Monitor {

	private static final int THRESHOLD_IN_CENTIMETRES = 3;
	
	private SensorManager sensorManager;
	private SensorEventListener listener;

	public ProximitySensorMonitor(final Context context, Listener listener, int timeoutMilliseconds) throws EnvironmentException {
		super(listener, timeoutMilliseconds);
		
		sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		this.listener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				int sensorType = event.sensor.getType();
				
				if (sensorType != Sensor.TYPE_PROXIMITY)
					throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
				
				float distanceInCentimetres = event.values[0];
				float coveredThreshold = Math.min(THRESHOLD_IN_CENTIMETRES, event.sensor.getMaximumRange());
				boolean covered = distanceInCentimetres <= coveredThreshold;
				if (covered)
					try {
						trigger();
					} catch (EnvironmentException e) {
						new UserErrorHandler(context).handle(e);
					}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		};
		
		Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (proximitySensor == null)
			throw new EnvironmentException("No proximity sensor");
		
		boolean registered = sensorManager.registerListener(this.listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		if (!registered)
			throw new EnvironmentException("Couldn't use sensor " + proximitySensor);
	}
	
	@Override
	public void stop() {
		sensorManager.unregisterListener(listener);
		super.stop();
	}
}
