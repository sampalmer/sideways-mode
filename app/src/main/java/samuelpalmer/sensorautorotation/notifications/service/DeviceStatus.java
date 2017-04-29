package samuelpalmer.sensorautorotation.notifications.service;

import android.support.annotation.StringRes;

import samuelpalmer.sensorautorotation.R;

public enum DeviceStatus {
	OK(0),
	WAITING_FOR_SENSOR(R.string.status_waiting_sensor),
	
	/**
	 * The device orientation needs to be determined before the screen orientation can be determined.
	 */
	WAITING_FOR_ORIENTATION(R.string.status_waiting_orientation);
	
	/**
	 * Or <code>0</code> if the status is normal.
	 */
	@StringRes public final int stringResource;
	
	DeviceStatus(@StringRes int stringResource) {
		this.stringResource = stringResource;
	}
}
