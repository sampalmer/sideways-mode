package samuelpalmer.sensorautorotation;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;

public abstract class LockScreenStatus {
	public static boolean isActive() {
		KeyguardManager keyguardManager = (KeyguardManager) ThisApplication.context().getSystemService(Context.KEYGUARD_SERVICE);
		if (Build.VERSION.SDK_INT >= 16)
			return keyguardManager.isKeyguardLocked();
		else
			return keyguardManager.inKeyguardRestrictedInputMode();
	}
}
