package samuelpalmer.sensorautorotation;

import android.service.notification.StatusBarNotification;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.rules.ApplicationCommandRule;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.ApplicationStatusRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.notification.NotificationRule;

public class WhenImUpright {

    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationCommandRule app = new ApplicationCommandRule();

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public ApplicationStatusRule appStatus = new ApplicationStatusRule();

    @Rule
    public NotificationRule notifications = new NotificationRule(testActivity);

    private String targetPackage;

    @Before
    public void setTargetPackage() {
        targetPackage = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
    }

    @Test
    public void hasNoNotificationBeforeAppStarted() {
        StatusBarNotification[] notifications = this.notifications.waitForNotificationsToSettle();

        boolean hasServiceNotification = false;
        for (StatusBarNotification notification : notifications)
            if (isServiceNotification(notification)) {
                hasServiceNotification = true;
                break;
            }

        Assert.assertThat(hasServiceNotification, CoreMatchers.is(false));
    }

    private boolean isServiceNotification(StatusBarNotification notification) {
        return notification.getPackageName().equals(targetPackage) && notification.getId() == NotificationIds.SERVICE;
    }

}
