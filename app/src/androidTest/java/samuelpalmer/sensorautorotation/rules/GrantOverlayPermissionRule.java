package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;
import android.os.Build;

import org.junit.rules.ExternalResource;

public class GrantOverlayPermissionRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();

        // The permission is automatically granted on earlier Android versions
        if (Build.VERSION.SDK_INT < 23)
            return;

        AppOpsHelper.grantAppOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW);
    }

}
