package samuelpalmer.sensorautorotation.notifications.service;

public enum DeviceStatus {
	OK,
	WAITING_FOR_SENSOR,
	
	/**
	 * The device orientation needs to be determined before the screen orientation can be determined.
	 */
	WAITING_FOR_ORIENTATION
}
