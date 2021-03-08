package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.IOException;

class AppOpsHelper {

    public static void grantAppOp(String op) {
        set(op, "allow");
    }

    public static void revokeAppOp(String op) {
        set(op, "default");
    }

    private static void set(String op, String mode) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        String targetPackageName = instrumentation.getTargetContext().getPackageName();

        String shellCommand = String.format("appops set %s %s %s", targetPackageName, op, mode);

        try {
            device.executeShellCommand(shellCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AppOpsHelper() {}

}
