package samuelpalmer.sensorautorotation;

import android.app.PendingIntent;

public abstract class PendingIntentIds {
	
	public static final int MAIN_NOTIFICATION_CLICK = 1;
	public static final int SERVICE_REVIVER = 2;
	public static final int FORCED_APPS_ACQUIRE_PERMISSION = 3;
	public static final int UNUSED1 = 4;
	public static final int UNUSED2 = 5;
	public static final int UNUSED3 = 6;
	public static final int UNUSED4 = 7;
	public static final int UNUSED5 = 8;
	public static final int ENVIRONMENT_ERROR_MESSAGE = 9;
	public static final int UPGRADE_MESSAGE = 10;
	public static final int MAIN_NOTIFICATION_CHANGE_SIDE = 11;
	public static final int MAIN_NOTIFICATION_TURN_OFF = 12;
	public static final int QUICK_SETTING_CLICK = 13;
	public static final int ENVIRONMENT_ERROR_NOTIFICATION_CLEAR = 14;
	
	// Always cancelling the existing pending intent since it's possible for the existing one to die and become permanently useless.
	public static final int PENDING_INTENT_FLAGS = PendingIntent.FLAG_CANCEL_CURRENT;
	
}
