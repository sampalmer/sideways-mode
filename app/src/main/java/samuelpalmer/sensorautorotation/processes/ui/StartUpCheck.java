package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;

import samuelpalmer.common.IntentExtensions;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;

public class StartUpCheck {
	
	/**
	 * Returns 0 on success. Otherwise returns explanation string resource.
	 */
	@StringRes
	public static int check() {
		if (!OrientationSensorHandler.canDetectOrientation())
			return R.string.device_problem_explanation_sensor;
		
		if (Build.VERSION.SDK_INT >= 23) {
			Intent modifySystemSettingsIntent = new SystemIntents(CurrentProcess.context()).modifySystemSettings();
			if (IntentExtensions.activityDoesntExist(modifySystemSettingsIntent, CurrentProcess.context().getPackageManager()))
				return R.string.device_problem_explanation_permission;
		}
		
		return 0;
	}
	
}
