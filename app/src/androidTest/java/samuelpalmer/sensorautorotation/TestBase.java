package samuelpalmer.sensorautorotation;

import org.junit.ClassRule;
import org.junit.Rule;

import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.GrantNotificationPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;

/**
 * Base class for all test classes. Provides rules that are common to all tests to avoid code duplication.
 */
public abstract class TestBase {

    @ClassRule
    public static GrantNotificationPermissionRule notificationPermission = new GrantNotificationPermissionRule();

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

}
