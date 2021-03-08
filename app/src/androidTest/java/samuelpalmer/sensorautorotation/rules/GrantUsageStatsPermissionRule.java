package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;

public class GrantUsageStatsPermissionRule extends RuntimePermissionRule {

    public GrantUsageStatsPermissionRule() {
        super(AppOpsManager.OPSTR_GET_USAGE_STATS, 21);
    }

}
