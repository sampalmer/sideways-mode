package samuelpalmer.sensorautorotation;

import android.app.Instrumentation;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.ExternalResource;

/**
 * Grants the target app permission to write system settings
 */
public class GrantSystemSettingPermissionRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();

        // The permission is automatically granted on earlier Android versions
        if (Build.VERSION.SDK_INT < 23)
            return;

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        String targetPackageName = instrumentation.getTargetContext().getPackageName();

        String shellCommand = String.format("appops set %s WRITE_SETTINGS allow", targetPackageName);
        device.executeShellCommand(shellCommand);
    }

}
