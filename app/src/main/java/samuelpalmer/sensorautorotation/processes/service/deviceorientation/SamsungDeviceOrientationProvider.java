package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import samuelpalmer.sensorautorotation.SystemServices;

class SamsungDeviceOrientationProvider implements DeviceOrientationProvider {

	public static final int TYPE_SCREEN_ORIENTATION = 65558;
	private final Sensor sensor;
	private final SensorManager sensorManager;
	private DeviceOrientationListener listener;

	public SamsungDeviceOrientationProvider() {
		sensorManager = SystemServices.sensorManager();
		sensor = sensorManager.getDefaultSensor(TYPE_SCREEN_ORIENTATION);
	}

	@Override
	public boolean supported() {
		return sensor != null;
	}

	@Override
	public void setListener(DeviceOrientationListener listener) {
		this.listener = listener;
	}

	@Override
	public void enable() {
		sensorManager.registerListener(
			subscription,
			sensor,
			//This sensor seems to only report when its value changes, so we might as well ask for the least lag
			SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void disable() {
		sensorManager.unregisterListener(subscription);
	}

	@Override
	public void setBias(int rotation) {

	}

	private final SensorEventListener subscription = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.values[0] != 255f)
				listener.changed((int) event.values[0]);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

}
