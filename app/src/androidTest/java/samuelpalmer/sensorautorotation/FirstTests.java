package samuelpalmer.sensorautorotation;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverride;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverrideReceiver;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.processes.ui.testing.TestActivity;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class FirstTests {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule grantPermissionRule = new GrantSystemSettingPermissionRule();

    private static final String BASIC_SAMPLE_PACKAGE = "samuelpalmer.sensorautorotation.dev";

    private static final int LAUNCH_TIMEOUT = 5000;

    private UiDevice device;
    private Context context;

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        Instrumentation instrumentation = getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        context = getApplicationContext();
        assertThat(device, notNullValue());
        assertThat(context, notNullValue());

        // Start test activity
        testActivity.getScenario().onActivity(activity -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));

        // Start dashboard
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        boolean launched = device.wait(Until.hasObject(By.clazz(DashboardActivity.class).depth(0)), LAUNCH_TIMEOUT);
        assertTrue("Failed to launch dashboard", launched);
    }

    @Test
    public void checkPreconditions() {
        SensorOverrideReceiver.sendOverride(context, SensorOverride.LEFT_SIDE);

        // Type text and then press the button.
        device.findObject(By.res(BASIC_SAMPLE_PACKAGE, "dashboard_button_left_side")).click();

        // Wasting time so we can visually check the result
        UiObject2 changedText = device
                .wait(Until.findObject(By.res(BASIC_SAMPLE_PACKAGE, "textToBeChanged")),
                        5000 /* wait 5000ms */);

        SensorOverrideReceiver.sendOverride(context, SensorOverride.OFF);
    }

}
