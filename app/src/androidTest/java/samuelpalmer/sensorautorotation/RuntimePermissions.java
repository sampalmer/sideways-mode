package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.GrantOverlayPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantUsageStatsPermissionRule;
import samuelpalmer.sensorautorotation.rules.RuntimePermissionRule;
import samuelpalmer.sensorautorotation.rules.notification.NotificationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

/**
 * Tests for when the user or operating system changes the app's permissions at runtime
 */
public class RuntimePermissions extends TestBase {

    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public NotificationRule notifications = new NotificationRule(testActivity);

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public GrantUsageStatsPermissionRule usagePermission = new GrantUsageStatsPermissionRule();

    @Rule
    public GrantOverlayPermissionRule drawOverAppsPermission = new GrantOverlayPermissionRule();

    @Test
    public void switchesOffWhenSystemSettingsPermissionRevoked() {
        Assume.assumeTrue("System setting permission cannot be revoked on this device", grantPermissionRule.isRuntimePermission());

        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        grantPermissionRule.revoke();

        ApplicationStatus newStatus;
        try {
            newStatus = app.waitForAppToStop();
        } catch (TimeoutException e) {
            Assert.fail("App did not stop after revoking system settings permission");
            return;
        }

        Assert.assertThat("App status did not have an error after revoking system settings permission", newStatus.error, CoreMatchers.notNullValue());

        boolean hasErrorNotification = notifications.hasNotification(NotificationIds.ERROR);
        Assert.assertTrue("App didn't raise error notification after revoking system settings permission", hasErrorNotification);
    }

    @Test
    public void alertsWhenUsagePermissionRevoked() {
        testOptionalPermissionRevocation(usagePermission, "usage");
    }

    @Test
    public void alertsWhenOverlayPermissionRevoked() {
        testOptionalPermissionRevocation(drawOverAppsPermission, "draw over other apps");
    }

    private void testOptionalPermissionRevocation(RuntimePermissionRule permission, String permissionName) {
        Assume.assumeTrue(String.format("%s permission cannot be revoked on this device", permissionName), permission.isRuntimePermission());

        // Marking an app as incompatible so the permissions will be monitored. Doesn't matter which app we mark.
        String testPackageName = InstrumentationRegistry.getInstrumentation().getContext().getPackageName();
        appSettings.markAsIncompatible(testPackageName);

        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);

        permission.revoke();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ApplicationStatus status = app.getCurrentStatus();
        Assert.assertThat(String.format("App stopped after revoking %s access", permissionName), status.runningState, CoreMatchers.notNullValue());

        boolean hasPermissionNotification = notifications.hasNotification(NotificationIds.PERMISSION);
        Assert.assertTrue(String.format("App didn't raise permission notification after revoking %s access", permissionName), hasPermissionNotification);
    }

}
