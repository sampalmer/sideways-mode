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
import samuelpalmer.sensorautorotation.rules.MockDeviceOrientationRule;
import samuelpalmer.sensorautorotation.rules.ScreenRotationRule;
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
    public MockDeviceOrientationRule device = new MockDeviceOrientationRule();

    @Rule
    public ApplicationCommandRule app = new ApplicationCommandRule();

    @Rule
    public ScreenRotationRule screen = new ScreenRotationRule();

    @Test
    public void userRightDeviceRight() {
        check(ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.RIGHT_SIDE, Orientation.NATURAL);
    }

    @Test
    public void userRightDeviceUpright() {
        check(ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.NATURAL, Orientation.RIGHT_SIDE);
    }

    @Test
    public void userRightDeviceLeft() {
        check(ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.LEFT_SIDE, Orientation.UPSIDE_DOWN);
    }

    @Test
    public void userRightDeviceUpsideDown() {
        check(ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.UPSIDE_DOWN, Orientation.LEFT_SIDE);
    }

    @Test
    public void userLeftDeviceRight() {
        check(ApplicationCommand.ACTION_LEFT_SIDE, Orientation.RIGHT_SIDE, Orientation.UPSIDE_DOWN);
    }

    @Test
    public void userLeftDeviceUpright() {
        check(ApplicationCommand.ACTION_LEFT_SIDE, Orientation.NATURAL, Orientation.LEFT_SIDE);
    }

    @Test
    public void userLeftDeviceLeft() {
        check(ApplicationCommand.ACTION_LEFT_SIDE, Orientation.LEFT_SIDE, Orientation.NATURAL);
    }

    @Test
    public void userLeftDeviceUpsideDown() {
        check(ApplicationCommand.ACTION_LEFT_SIDE, Orientation.UPSIDE_DOWN, Orientation.RIGHT_SIDE);
    }

    private void check(@ApplicationCommand.ACTION String userSide, Orientation deviceOrientation, Orientation expectedScreenRotation) {
        device.setRotation(deviceOrientation);
        app.run(userSide);
        Orientation screenRotation = screen.waitForRotation(expectedScreenRotation);

        Assert.assertThat(screenRotation, CoreMatchers.is(expectedScreenRotation));
    }

}
