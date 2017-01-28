package samuelpalmer.sensorautorotation.processes.service;

import android.annotation.TargetApi;
import android.os.Build;

import samuelpalmer.sensorautorotation.notifications.service.ServiceStatus;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusDisplay;


@TargetApi(Build.VERSION_CODES.N)
public class QuickSettingStatusDisplay implements ServiceStatusDisplay {

	@Override
	public void onServiceStopped() {
		QuickSettingService.requestTileUpdate(null);
	}

	@Override
	public void update(ServiceStatus serviceStatus) {
		QuickSettingService.requestTileUpdate(serviceStatus);
	}

}
