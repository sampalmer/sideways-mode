package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.concurrent.TimeoutException;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.lockscreen.LockScreenRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;
import samuelpalmer.sensorautorotation.values.UserSide;

public class WhenScreenLocked {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    private final ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);
    private final LockScreenRule lockScreen = LockScreenRule.create(testActivity);

    @Rule
    public RuleChain activityAndLockScreen = RuleChain.outerRule(testActivity).around(lockScreen);

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Test
    public void turnsOffIfSettingOn() {
        appSettings.setTurnOffWhenLocked(true);
        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);

        lockScreen.enableLockScreen();
        lockScreen.lockDevice();

        try {
            app.waitForAppToStop();
        } catch (TimeoutException e) {
            Assert.fail("App did not turn off");
        }
    }

    @Test
    public void staysOnIfSettingOff() {
        appSettings.setTurnOffWhenLocked(false);
        ApplicationStatus startedStatus = app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);

        assert startedStatus.runningState != null;
        UserSide originalSide = startedStatus.runningState.userSide;

        lockScreen.enableLockScreen();
        lockScreen.lockDevice();

        // Also unlocking the device since sometimes the app doesn't detect the lock until the
        // subsequent unlock occurs
        lockScreen.unlockDevice(testActivity);

        // Allowing time for the app to notice that the device was locked and react to it
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ApplicationStatus newStatus = app.getCurrentStatus();
        Assert.assertThat("App turned off", newStatus.runningState, CoreMatchers.notNullValue());

        UserSide newSide = newStatus.runningState.userSide;
        Assert.assertThat("App side changed", newSide, CoreMatchers.equalTo(originalSide));
    }

}
