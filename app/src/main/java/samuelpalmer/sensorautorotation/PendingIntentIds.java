package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.PendingIntent;

@SuppressWarnings("unused") // Need to keep old pending intent ids around since they might persist across app upgrades.
public abstract class PendingIntentIds {
    
    public static final int MAIN_NOTIFICATION_CLICK = 1;
    public static final int UNUSED1 = 2;
    public static final int UNUSED2 = 3;
    public static final int UNUSED3 = 4;
    public static final int UNUSED4 = 5;
    public static final int UNUSED5 = 6;
    public static final int UNUSED6 = 7;
    public static final int UNUSED7 = 8;
    public static final int ENVIRONMENT_ERROR_MESSAGE = 9;
    public static final int UPGRADE_MESSAGE = 10;
    public static final int MAIN_NOTIFICATION_LEFT_SIDE = 11;
    public static final int MAIN_NOTIFICATION_TURN_OFF = 12;
    public static final int QUICK_SETTING_CLICK = 13;
    public static final int ENVIRONMENT_ERROR_NOTIFICATION_CLEAR = 14;
    public static final int MAIN_NOTIFICATION_RIGHT_SIDE = 15;
    public static final int TASK_REMOVAL_FIX = 16;

    // FLAG_IMMUTABLE is only available on API 23, but if we add conditional logic then a warning is
    // produced for every use of this field
    @SuppressLint("InlinedApi")
    public static final int PENDING_INTENT_FLAGS = PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE;

}
