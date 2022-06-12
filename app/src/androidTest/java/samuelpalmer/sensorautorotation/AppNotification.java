package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.notification.NotificationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Tests related to the main service notification
 */
public class AppNotification extends TestBase {

    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public NotificationRule notifications = new NotificationRule(testActivity);

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
        boolean hasServiceNotification = notifications.hasNotification(NotificationIds.SERVICE);
        Assert.assertThat(shouldHaveNotification ? "Service notification is missing" : "Service notification is present", hasServiceNotification, CoreMatchers.is(shouldHaveNotification));
    }

}
