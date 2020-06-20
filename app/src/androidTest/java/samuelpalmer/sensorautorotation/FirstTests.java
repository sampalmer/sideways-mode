package samuelpalmer.sensorautorotation;

import android.app.Instrumentation;
import android.view.Surface;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import samuelpalmer.sensorautorotation.processes.ui.testing.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationCommandRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.deviceorientation.DeviceOrientationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

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

    @Test
    public void invertsScreenWhenDeviceUpsideDown() throws InterruptedException {
        deviceOrientation.setRightSide();
        command.run(ApplicationCommand.ACTION_LEFT_SIDE);

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);

        // Need to wait for screen orientation to change
        Thread.sleep(5000);

        int displayRotation = device.getDisplayRotation();
        Assert.assertThat(displayRotation, CoreMatchers.is(Surface.ROTATION_180));
    }

}
