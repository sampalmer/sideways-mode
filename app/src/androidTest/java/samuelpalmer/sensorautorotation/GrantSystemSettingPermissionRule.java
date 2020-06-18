package samuelpalmer.sensorautorotation;

import android.app.Instrumentation;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Grants the target app permission to write system settings
 */
public class GrantSystemSettingPermissionRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new GrantSystemSettingPermissionStatement(base);
    }

    private static class GrantSystemSettingPermissionStatement extends Statement {

        private final Statement base;

        private GrantSystemSettingPermissionStatement(Statement base) {
            this.base = base;
        }

        @Override
        public void evaluate() throws Throwable {
            Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
            UiDevice device = UiDevice.getInstance(instrumentation);

            String targetPackageName = instrumentation.getTargetContext().getPackageName();

            // The permission is automatically granted on earlier Android versions
            if (Build.VERSION.SDK_INT >= 23) {
                String shellCommand = String.format("appops set %s WRITE_SETTINGS allow", targetPackageName);
                device.executeShellCommand(shellCommand);
            }

            base.evaluate();
        }

    }

}
