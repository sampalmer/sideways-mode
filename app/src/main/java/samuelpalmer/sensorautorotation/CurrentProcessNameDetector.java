package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class CurrentProcessNameDetector {

    public static String getName() {
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
