package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.MockDeviceOrientationRule;
import samuelpalmer.sensorautorotation.rules.ScreenRotationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.Orientation;

@RunWith(Parameterized.class)
public class ScreenRotation {

    private final Orientation expectedScreen;
    private final String user;
    private final Orientation deviceRotation;

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    @Rule
    public MockDeviceOrientationRule device = new MockDeviceOrientationRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Rule
    public ScreenRotationRule screen = new ScreenRotationRule();

    @Parameterized.Parameters(name = "When user {0} and device {1} then screen {2}")
    public static Object[][] data() {
        return new Object[][] {
                { ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.RIGHT_SIDE, Orientation.NATURAL },
                { ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.NATURAL, Orientation.RIGHT_SIDE },
                { ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.LEFT_SIDE, Orientation.UPSIDE_DOWN },
                { ApplicationCommand.ACTION_RIGHT_SIDE, Orientation.UPSIDE_DOWN, Orientation.LEFT_SIDE },
                { ApplicationCommand.ACTION_LEFT_SIDE, Orientation.RIGHT_SIDE, Orientation.UPSIDE_DOWN },
                { ApplicationCommand.ACTION_LEFT_SIDE, Orientation.NATURAL, Orientation.LEFT_SIDE },
                { ApplicationCommand.ACTION_LEFT_SIDE, Orientation.LEFT_SIDE, Orientation.NATURAL },
                { ApplicationCommand.ACTION_LEFT_SIDE, Orientation.UPSIDE_DOWN, Orientation.RIGHT_SIDE },
        };
    }

    public ScreenRotation(@ApplicationCommand.ACTION String user, Orientation device, Orientation expectedScreen) {
        this.expectedScreen = expectedScreen;
        this.user = user;
        deviceRotation = device;
    }

    @Test
    public void test() {
        device.setRotation(deviceRotation);
        app.startApp(user);
        Orientation screenRotation = screen.waitForScreenToSettle();

        String message = "Wrong screen orientation when user " + user + " and device " + deviceRotation;
        Assert.assertThat(message, screenRotation, CoreMatchers.is(expectedScreen));
    }

}
