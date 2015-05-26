package samuelpalmer.sensorautorotation.notifications;

import android.os.Build;

public abstract class NotificationTimestamp {
	public static long highest() {
		long result = System.currentTimeMillis();

		if (Build.VERSION.SDK_INT < 21) //Because Android 5 actually ignores future 'when' values ordering and even doesn't update the notification time when they're future
			result += millisecondsFromYears(10L); //Can't go too high (eg Integer.MAX_VALUE) since that seems to cause the opposite effect of what we want (at least in Android 4.3)

		return result;
	}

	private static long millisecondsFromYears(long years) {
		return years * 365L * 24L * 60L * 60L * 1000L;
	}
}
