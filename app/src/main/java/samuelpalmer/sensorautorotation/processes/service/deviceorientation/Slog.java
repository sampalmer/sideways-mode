package samuelpalmer.sensorautorotation.processes.service.deviceorientation;

import android.util.Log;

/**
 * Wrapper around {@link Log} for use by AOSP code in {@link WindowOrientationListener}.
 * Using this makes it easier to diff the custom implementation of {@link WindowOrientationListener}
 * against the AOSP code.
 */
class Slog {
    public static void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    public static void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @SuppressWarnings("unused")
    public static void wtf(String tag, String msg) {
        Log.wtf(tag, msg);
    }
}
