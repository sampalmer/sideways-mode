package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import java.io.IOException;

class AppOpsHelper {

    public static void grantAppOp(String op) throws IOException {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        String targetPackageName = instrumentation.getTargetContext().getPackageName();

        String shellCommand = String.format("appops set %s %s allow", targetPackageName, op);
        device.executeShellCommand(shellCommand);
    }

    private AppOpsHelper() {}

}
