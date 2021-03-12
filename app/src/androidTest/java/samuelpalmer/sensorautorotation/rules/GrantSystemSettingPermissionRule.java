package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;

/**
 * Grants the target app permission to write system settings
 */
public class GrantSystemSettingPermissionRule extends RuntimePermissionRule {

    public GrantSystemSettingPermissionRule() {
        super(AppOpsManager.OPSTR_WRITE_SETTINGS, 23);
    }

}
