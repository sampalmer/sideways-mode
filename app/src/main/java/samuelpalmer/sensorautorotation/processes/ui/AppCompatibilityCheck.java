package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.StringRes;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.utilities.IntentExtensions;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;

public class AppCompatibilityCheck {
	
	/**
	 * Determines whether the app will work on the current device.
	 * Returns 0 on success. Otherwise returns explanation string resource.
	 */
	@StringRes
	public static int check() {
		if (!OrientationSensorHandler.canDetectOrientation())
			return R.string.device_problem_explanation_sensor;
		
		if (Build.VERSION.SDK_INT >= 23) {
			Intent modifySystemSettingsIntent = SystemIntents.modifySystemSettings(CurrentProcess.context());
			if (IntentExtensions.activityDoesntExist(modifySystemSettingsIntent, CurrentProcess.context().getPackageManager()))
				return R.string.device_problem_explanation_permission;
		}
		
		return 0;
	}
	
}
