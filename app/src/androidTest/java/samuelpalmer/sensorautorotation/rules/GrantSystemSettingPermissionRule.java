package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;
import android.os.Build;

import org.junit.rules.ExternalResource;

/**
 * Grants the target app permission to write system settings
 */
public class GrantSystemSettingPermissionRule extends ExternalResource {

    private boolean granted;

    @Override
    protected void before() throws Throwable {
        super.before();

        if (!isRuntimePermission())
            return;

        AppOpsHelper.grantAppOp(AppOpsManager.OPSTR_WRITE_SETTINGS);
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
    }

    public boolean isRuntimePermission() {
        // The permission is automatically granted on earlier Android versions
        return Build.VERSION.SDK_INT >= 23;
    }

    public void revoke() {
        if (!isRuntimePermission())
            throw new IllegalStateException("Cannot revoke because this is not a runtime permission on this device");

        if (!granted)
            throw new IllegalStateException("Permission is already revoked");

        AppOpsHelper.revokeAppOp(AppOpsManager.OPSTR_WRITE_SETTINGS);
        granted = false;
    }

}
