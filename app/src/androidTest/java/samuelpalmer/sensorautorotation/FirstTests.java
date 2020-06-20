package samuelpalmer.sensorautorotation;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import samuelpalmer.sensorautorotation.processes.ui.testing.TestActivity;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.deviceorientation.DeviceOrientationRule;

@RunWith(AndroidJUnit4.class)
public class FirstTests {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    @Rule
    public DeviceOrientationRule deviceOrientation = DeviceOrientationRule.create();

    @Test
    public void firstTest() {
        deviceOrientation.setRightSide();

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // TODO: Implement tests
    }

}
