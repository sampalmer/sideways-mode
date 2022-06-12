package samuelpalmer.sensorautorotation.rules;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.test.rule.GrantPermissionRule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Grants permission to post notifications. Stays in effect until entire test run is complete.
 */
public class GrantNotificationPermissionRule implements TestRule {

    private final TestRule permissionRule;

    public GrantNotificationPermissionRule() {
        if (Build.VERSION.SDK_INT >= 33) {
            permissionRule = GrantPermissionRule.grant("android.permission.POST_NOTIFICATIONS");
        } else {
            permissionRule = (base, description) -> base;
        }
    }

    @NonNull
    @Override
    public Statement apply(@NonNull Statement base, @NonNull Description description) {
        return permissionRule.apply(base, description);
    }

}
