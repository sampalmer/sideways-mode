package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import samuelpalmer.sensorautorotation.SystemServices;

/**
 * Has much less lag than the AOSP mechanism while still being very accurate.
 *
 * Only tested on Samsung Galaxy S5, but Google searches suggest the sensor is present in other devices:
 * -Galaxy Note 2 (https://github.com/hbibel/TapToUnlock/wiki/Sensor-Dumps-of-Different-Devices)
 * -Galaxy Tab (http://androidfragmentation.com/database/v/dr/688354/oem/samsung/dm/SM-T310)
 * -Galaxy A5 (http://androidfragmentation.com/database/v/dr/688592/oem/samsung/dm/SM-A500H)
 * -Galaxy Tab 3 (http://androidfragmentation.com/database/v/dr/688547/oem/samsung/dm/SM-T211)
 * -Galaxy Grand 2 (http://androidfragmentation.com/database/v/dr/657556/oem/samsung/dm/SM-G7102)
 */
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
		return
			sensor != null
			&&
			sensor.getVendor().toLowerCase().contains("samsung")
			&&
			sensor.getName().toLowerCase().contains("screen orientation");
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
