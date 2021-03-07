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
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class AppNotification {

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
    public void doesntShowBeforeAppStarted() {
        assetHasNotification(false);
    }

    @Test
    public void showsAfterAppStarted() {
        startApp();
        assetHasNotification(true);
    }

    @Test
    public void doesntShowAfterAppStopped() {
        startApp();
        stopApp();
        assetHasNotification(false);
    }

    @Test
    public void showsBeforeAppStartedIfUserPrefers() {
        appSettings.setCloseNotificationWhenUpright(false);
        assetHasNotification(true);
    }

    @Test
    public void showsAfterAppStoppedIfUserPrefers() {
        startApp();
        appSettings.setCloseNotificationWhenUpright(false);
        stopApp();
        assetHasNotification(true);
    }

    private void assetHasNotification(boolean shouldHaveNotification) {
        StatusBarNotification[] notifications = this.notifications.waitForNotificationsToSettle();
        boolean hasServiceNotification = hasServiceNotification(notifications);
        Assert.assertThat(hasServiceNotification, CoreMatchers.is(shouldHaveNotification));
    }

    private void startApp() {
        app.run(ApplicationCommand.ACTION_RIGHT_SIDE);
        ApplicationStatus applicationStatus = appStatus.waitForStatusChange();
        if (applicationStatus.runningState == null)
            throw new RuntimeException("App failed to start");
    }

    private void stopApp() {
        app.run(ApplicationCommand.ACTION_TURN_OFF);
        ApplicationStatus applicationStatus = appStatus.waitForStatusChange();
        if (applicationStatus.runningState != null)
            throw new RuntimeException("App failed to stop");
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
