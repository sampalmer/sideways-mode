package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Identifies which process the calling code is running in
 */
public final class CurrentProcessNameDetector {

    // These values are all cached to reduce lag, although this mightn't make any discernible difference
    private final String processName;
    private final String serviceProcessName;
    private final String uiProcessName;

    public CurrentProcessNameDetector(Context context) {
        processName = getName();

        String packageName = context.getPackageName();
        serviceProcessName = packageName + context.getText(R.string.process_service);
        uiProcessName = packageName + context.getText(R.string.process_ui);
    }

    /**
     * Returns the name of the process that the calling code is running in
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Returns whether the calling code is running in the service process
     */
    public boolean isServiceProcess() {
        if (processName.equals(serviceProcessName))
            return true;
        else if (processName.equals(uiProcessName))
            return false;
        else
            throw new RuntimeException("Unrecognised process name: " + processName);
    }

    private static String getName() {
        if (Build.VERSION.SDK_INT >= 28)
            return Application.getProcessName();
        else {
            // This does the same thing as Application.getProcessName() but for older OS versions

            try {
                @SuppressLint("PrivateApi")
                Class<?> activityThread = Class.forName("android.app.ActivityThread");

                // Before API 18, the method was incorrectly named "currentPackageName", but it still returned the process name
                // See https://github.com/aosp-mirror/platform_frameworks_base/commit/b57a50bd16ce25db441da5c1b63d48721bb90687
                String methodName = Build.VERSION.SDK_INT >= 18 ? "currentProcessName" : "currentPackageName";

                Method getProcessName = activityThread.getDeclaredMethod(methodName);
                return (String) getProcessName.invoke(null);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
