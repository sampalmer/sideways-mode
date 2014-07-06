package samuelpalmer.sensorautorotation;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class SensorDataHandler implements SensorEventListener {
	
	private final OrientationCalculator orientationCalculator;
	private final OrientationChanger orientationChanger;
	private final LowPassFilter gravity;

	public SensorDataHandler(OrientationCalculator orientationCalculator, OrientationChanger orientationChanger, Clock clock) {
		this.orientationCalculator = orientationCalculator;
		this.orientationChanger = orientationChanger;
		this.gravity = new LowPassFilter(clock);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		
		switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				updateOrientation(event);
				break;
			case Sensor.TYPE_PROXIMITY:
				float distance = event.values[0];
				boolean covered = distance < event.sensor.getMaximumRange();
				orientationCalculator.registerNewCoveredState(covered);
				break;
			default:
				throw new RuntimeException("Unexpected sensor: " + event.sensor.getName());
		}
		
		int screenOrientation = orientationCalculator.getScreenOrientation();
		orientationChanger.set(screenOrientation);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	
	private void updateOrientation(SensorEvent event) {
		final int threshold = 1;
		
		float[] components = gravity.process(event.values);
		float x = components[0];
		float y = components[1];
		
		float xMagnitude = Math.abs(x);
		float yMagnitude = Math.abs(y);
		
		float differenceInMagnitudes = Math.abs(xMagnitude - yMagnitude);
		if (differenceInMagnitudes > threshold) {
			boolean isMoreVertical = yMagnitude > xMagnitude;
			
			int deviceOrientation;
			
			if (isMoreVertical && yMagnitude > threshold)
				if (y > 0)
					deviceOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				else
					deviceOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
			else if (xMagnitude > threshold)
				if (x > 0)
					deviceOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				else
					deviceOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
			else
				return;
			
			orientationCalculator.registerNewDeviceOrientation(deviceOrientation);
		}
	}
}
