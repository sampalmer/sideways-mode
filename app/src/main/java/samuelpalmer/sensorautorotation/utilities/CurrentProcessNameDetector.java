package samuelpalmer.sensorautorotation.utilities;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Process;
import android.util.Log;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public abstract class CurrentProcessNameDetector {

    private static final String TAG = CurrentProcessNameDetector.class.getSimpleName();

    public static boolean isServiceProcess(Context context) {
        String commandLineProcessName = getNameFromCommandLine();
        if (commandLineProcessName != null) {
            Boolean isServiceProcess = isServiceProcess(context, commandLineProcessName);
            if (isServiceProcess != null)
                return isServiceProcess;
            else
                Log.w(TAG, "Got unexpected process name from command line: " + commandLineProcessName);
        }

        String activityManagerProcessName = getNameFromActivityManager(context);
        if (activityManagerProcessName != null) {
            Boolean isServiceProcess = isServiceProcess(context, activityManagerProcessName);
            if (isServiceProcess != null)
                return isServiceProcess;
            else
                Log.w(TAG, "Got unexpected process name from ActivityManager: " + activityManagerProcessName);
        }

        throw new RuntimeException("Couldn't identify the current process");
    }

    private static Boolean isServiceProcess(Context context, String currentProcessName) {
        String packageName = context.getPackageName();

        if (currentProcessName.equals(packageName + context.getText(R.string.process_service))) {
            return true; // Service process
        }
        else if (currentProcessName.equals(packageName + context.getText(R.string.process_ui))) {
            return false; // UI process
        }
        else {
            // It's possible for an the service name to be incorrectly detected,
            // so instead of crashing, we'll let the caller decide what to do.
            // See https://stackoverflow.com/a/21389402/238753.
            return null;
        }
    }

    /**
     * This performs a little faster than the ActivityManager technique.
     * However, apparently this sometimes returns an invalid process name,
     * such as when running Xposed.
     * This appears to have occurred on a Galaxy J2 (j2lte)    running Android 5.1
     * and a Galaxy J1 (2016) (j1xlte) running Android 5.1.
     *
     * See http://stackoverflow.com/a/21389402/238753
     */
    @Nullable
    private static String getNameFromCommandLine() {
        BufferedReader cmdlineReader = null;
        try {
            //noinspection CharsetObjectCanBeUsed because the Charset object is only available in API 19+
            cmdlineReader = new BufferedReader(
                new InputStreamReader(new FileInputStream("/proc/" + Process.myPid() + "/cmdline"), "iso-8859-1")
            );

            StringBuilder processName = new StringBuilder();
            while (true) {
                int c = cmdlineReader.read();
                if (c <= 0)
                    break;

                processName.append((char) c);
            }

            String result = processName.toString().trim();
            if (result.isEmpty()) {
                Log.w(TAG, "Got empty process name.");
                return null;
            }

            return result;
        } catch (Exception e) {
            Log.w(TAG, e);
            return null;
        } finally {
            if (cmdlineReader != null)
                try {
                    cmdlineReader.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
        }
    }

    /**
     * This approach seems generally reliable, and is the official way to do it.
     * However, someone claimed that it sometimes doesn't find your process: https://stackoverflow.com/a/21389402/238753
     */
    @Nullable
    private static String getNameFromActivityManager(Context context) {
        int pid = Process.myPid();

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses != null) // Apparently it's possible for getRunningAppProcesses() to return null: https://stackoverflow.com/questions/19631894/is-there-a-way-to-get-current-process-name-in-android#comment60629994_19632382
            for (RunningAppProcessInfo process : runningAppProcesses)
                if (process.pid == pid) {
                    String result = process.processName;
                    if (result == null)
                        Log.w(TAG, "Current process name was null");

                    return result;
                }

        Log.w(TAG, "Couldn't find current process name. Process ID: " + pid);
        return null;
    }

}
