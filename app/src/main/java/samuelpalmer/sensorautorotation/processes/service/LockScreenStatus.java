package samuelpalmer.sensorautorotation.processes.service;

import android.os.Build;

import samuelpalmer.sensorautorotation.SystemServices;

public abstract class LockScreenStatus {
	public static boolean isActive() {
		if (Build.VERSION.SDK_INT >= 16)
			return SystemServices.keyguardManager().isKeyguardLocked();
		else
			return SystemServices.keyguardManager().inKeyguardRestrictedInputMode();
	}
}
