package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;
import android.os.Build;

import org.junit.rules.ExternalResource;

public class GrantUsageStatsPermissionRule extends ExternalResource {

    @Override
    protected void before() throws Throwable {
        super.before();

        // The permission isn't applicable on earlier Android versions
        if (Build.VERSION.SDK_INT < 21)
            return;

        AppOpsHelper.grantAppOp(AppOpsManager.OPSTR_GET_USAGE_STATS);
    }

}
