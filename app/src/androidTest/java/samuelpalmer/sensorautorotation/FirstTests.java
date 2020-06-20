package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import samuelpalmer.sensorautorotation.processes.ui.testing.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationCommandRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.ScreenRotationRule;
import samuelpalmer.sensorautorotation.rules.deviceorientation.DeviceOrientationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.Orientation;

@RunWith(AndroidJUnit4.class)
public class FirstTests {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    @Rule
    public DeviceOrientationRule deviceOrientation = DeviceOrientationRule.create();

    @Rule
    public ApplicationCommandRule command = new ApplicationCommandRule();

    @Rule
    public ScreenRotationRule screen = new ScreenRotationRule();

    @Test
    public void invertsScreenWhenDeviceUpsideDown() {
        deviceOrientation.setRightSide();
        command.run(ApplicationCommand.ACTION_LEFT_SIDE);
        Orientation screenRotation = screen.waitForRotationChange();

        Assert.assertThat(screenRotation, CoreMatchers.is(Orientation.UPSIDE_DOWN));
    }

}
