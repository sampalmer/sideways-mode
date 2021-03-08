package samuelpalmer.sensorautorotation.rules;

import android.app.AppOpsManager;

public class GrantOverlayPermissionRule extends RuntimePermissionRule {

    public GrantOverlayPermissionRule() {
        super(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, 23);
    }

}
