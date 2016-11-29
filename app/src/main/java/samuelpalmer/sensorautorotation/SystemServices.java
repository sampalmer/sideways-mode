package samuelpalmer.sensorautorotation;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;

import static android.content.Context.ACCESSIBILITY_SERVICE;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

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

	public static SensorManager sensorManager() {
		return (SensorManager) get(SENSOR_SERVICE);
	}

	public static AccessibilityManager accessibilityManager() {
		return (AccessibilityManager) get(ACCESSIBILITY_SERVICE);
	}

	private static Object get(String name) {
		return CurrentProcess.context().getSystemService(name);
	}

	public static PackageManager packageManager() {
		return CurrentProcess.context().getPackageManager();
	}
}
