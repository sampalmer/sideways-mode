package samuelpalmer.sensorautorotation;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.os.PowerManager;
import android.view.WindowManager;

import static android.content.Context.*;

@SuppressWarnings("unused")
public abstract class SystemServices {
	public static WindowManager windowManager() {
		return (WindowManager) get(WINDOW_SERVICE);
	}

	public static NotificationManager notificationManager() {
		return (NotificationManager) get(NOTIFICATION_SERVICE);
	}

	public static KeyguardManager keyguardManager() {
		return (KeyguardManager) get(KEYGUARD_SERVICE);
	}

	public static AlarmManager alarmManager() {
		return (AlarmManager) get(ALARM_SERVICE);
	}

	public static ActivityManager activityManager() {
		return (ActivityManager) get(ACTIVITY_SERVICE);
	}

	public static PowerManager powerManager() {
		return (PowerManager) get(POWER_SERVICE);
	}

	private static Object get(String name) {
		return Process.context().getSystemService(name);
	}
}
