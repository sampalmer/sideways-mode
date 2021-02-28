package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationCommandRule;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.ApplicationStatusRule;
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
    public ApplicationCommandRule app = new ApplicationCommandRule();

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public ApplicationStatusRule appStatus = new ApplicationStatusRule();

    @Test
    public void turnsOffIfSettingOn() {
        appSettings.setTurnOffWhenLocked(true);
        app.run(ApplicationCommand.ACTION_RIGHT_SIDE);

        ApplicationStatus status = appStatus.waitForStatusChange();
        Assert.assertThat("App did not start", status.runningState, CoreMatchers.notNullValue());

        lockScreen.enableLockScreen();
        lockScreen.lockDevice();

        status = appStatus.waitForStatusChange();
        Assert.assertThat("App did not turn off", status.runningState, CoreMatchers.nullValue());
    }

    @Test
    public void staysOnIfSettingOff() {
        appSettings.setTurnOffWhenLocked(false);
        app.run(ApplicationCommand.ACTION_RIGHT_SIDE);

        ApplicationStatus status = appStatus.waitForStatusChange();
        Assert.assertThat("App did not start", status.runningState, CoreMatchers.notNullValue());
        UserSide originalSide = status.runningState.userSide;

        lockScreen.enableLockScreen();
        lockScreen.lockDevice();

        status = appStatus.waitForStatusChange();
        Assert.assertThat("App turned off", status.runningState, CoreMatchers.notNullValue());

        UserSide newSide = status.runningState.userSide;
        Assert.assertThat("App side changed", newSide, CoreMatchers.equalTo(originalSide));
    }

}
