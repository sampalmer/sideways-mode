package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.notification.NotificationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class RuntimePermissions {

    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Rule
    public NotificationRule notifications = new NotificationRule(testActivity);

    @Test
    public void switchesOffWhenSystemSettingsRevoked() {
        Assume.assumeTrue("System setting permission cannot be revoked on this device", grantPermissionRule.isRuntimePermission());

        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        grantPermissionRule.revoke();

        ApplicationStatus newStatus;
        try {
            newStatus = app.waitForAppState(false);
        } catch (TimeoutException e) {
            Assert.fail("App did not stop after revoking system settings permission");
            return;
        }

        Assert.assertThat("App status did not have an error after revoking system settings permission", newStatus.error, CoreMatchers.notNullValue());

        boolean hasErrorNotification = notifications.hasNotification(NotificationIds.ERROR);
        Assert.assertTrue("App didn't raise error notification after revoking system settings permission", hasErrorNotification);
    }

}
