package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.os.Build;

import samuelpalmer.sensorautorotation.notifications.service.NotificationParameters;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplay;


@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingStatusDisplay implements ServiceStatusDisplay {

	@Override
	public void update(NotificationParameters params) {
		//TODO: Have the quick setting service hang onto "params" to save recreated it unnecessarily.
		QuickSettingService.requestTileUpdate();
	}

}
