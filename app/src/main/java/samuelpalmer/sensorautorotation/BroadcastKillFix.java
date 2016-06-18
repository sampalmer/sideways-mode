package samuelpalmer.sensorautorotation;

import android.content.Intent;
import android.os.Build;

abstract class BroadcastKillFix {
	/**
	 * Prevents the receiving process from being killed by the OS when it receives the broadcast
	 * after task removal in Android 4.1 and above.
	 */
	public static void apply(Intent intent) {
		if (Build.VERSION.SDK_INT >= 16)
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
	}
}
