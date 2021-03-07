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
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.notification.NotificationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public class AppNotification {

    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public NotificationRule notifications = new NotificationRule(testActivity);

    private String targetPackage;

    @Before
    public void setTargetPackage() {
        targetPackage = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
    }

    @Test
    public void doesntShowBeforeAppStarted() {
        assertHasNotification(false);
    }

    @Test
    public void showsAfterAppStarted() {
        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        assertHasNotification(true);
    }

    @Test
    public void doesntShowAfterAppStopped() {
        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        app.stopApp();
        assertHasNotification(false);
    }

    @Test
    public void showsBeforeAppStartedIfUserPrefers() {
        appSettings.setCloseNotificationWhenUpright(false);
        assertHasNotification(true);
    }

    @Test
    public void showsAfterAppStoppedIfUserPrefers() {
        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        appSettings.setCloseNotificationWhenUpright(false);
        app.stopApp();
        assertHasNotification(true);
    }

    private void assertHasNotification(boolean shouldHaveNotification) {
        StatusBarNotification[] notifications = this.notifications.waitForNotificationsToSettle();
        boolean hasServiceNotification = hasServiceNotification(notifications);
        Assert.assertThat(shouldHaveNotification ? "Service notification is missing" : "Service notification is present", hasServiceNotification, CoreMatchers.is(shouldHaveNotification));
    }

    private boolean hasServiceNotification(StatusBarNotification[] notifications) {
        for (StatusBarNotification notification : notifications) {
            boolean isServiceNotification = notification.getPackageName().equals(targetPackage) && notification.getId() == NotificationIds.SERVICE;
            if (isServiceNotification)
                return true;
        }

        return false;
    }

}
