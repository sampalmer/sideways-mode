package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.os.Build;

import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplay;


@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingStatusDisplay implements ServiceStatusDisplay {

	@Override
	public void onServiceStopped() {
		QuickSettingService.requestTileUpdate();
	}

	@Override
	public void update(ServiceStatus serviceStatus) {
		//TODO: Have the quick setting service hang onto "serviceStatus" to save recreated it unnecessarily.
		QuickSettingService.requestTileUpdate();
	}

}
