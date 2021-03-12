package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;
import android.app.Instrumentation;
import android.os.Build;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.ExternalResource;

import java.io.IOException;

public abstract class RuntimePermissionRule extends ExternalResource {

    private final String op;
    private final int minSdkVersion;

    private UiDevice device;
    private String targetPackageName;
    private boolean granted;

    /**
     * @param op One of the {@link AppOpsManager} {@code OPSTR_} constants
     * @param minSdkVersion The minimum API level that supports this op as a runtime permission
     */
    RuntimePermissionRule(String op, int minSdkVersion) {
        this.op = op;
        this.minSdkVersion = minSdkVersion;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        if (!isRuntimePermission())
            return;

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        targetPackageName = instrumentation.getTargetContext().getPackageName();

        setOpMode(op, "allow");
        granted = true;
    }

    @Override
    protected void after() {
        super.after();

        if (!isRuntimePermission())
            return;

        if (granted) {
            revoke();
        }

        targetPackageName = null;
        device = null;
    }

    /**
     * Whether this permission is a runtime permission on the current device
     */
    public boolean isRuntimePermission() {
        // The permission is automatically granted on earlier Android versions
        return Build.VERSION.SDK_INT >= minSdkVersion;
    }

    public void revoke() {
        if (!isRuntimePermission())
            throw new IllegalStateException("Cannot revoke because this is not a runtime permission on this device");

        if (!granted)
            throw new IllegalStateException("Permission is already revoked");

        setOpMode(op, "default");
        granted = false;
    }

    private void setOpMode(String op, String mode) {
        String shellCommand = String.format("appops set %s %s %s", targetPackageName, op, mode);

        try {
            device.executeShellCommand(shellCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
