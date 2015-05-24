package samuelpalmer.sensorautorotation;

import android.os.Build;

public abstract class LockScreenStatus {
	public static boolean isActive() {
		if (Build.VERSION.SDK_INT >= 16)
			return SystemServices.keyguardManager().isKeyguardLocked();
		else
			return SystemServices.keyguardManager().inKeyguardRestrictedInputMode();
	}
}
