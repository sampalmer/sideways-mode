package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverride;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.SensorOverrideReceiver;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class FirstTests {

    private static final String BASIC_SAMPLE_PACKAGE = "samuelpalmer.sensorautorotation.dev";

    private static final int LAUNCH_TIMEOUT = 5000;

    private static final String STRING_TO_BE_TYPED = "UiAutomator";

    private UiDevice device;
    private Context context;

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(getInstrumentation());
        assertThat(device, notNullValue());

        // Start from the home screen
        device.pressHome();

        // Launch the blueprint app
        context = getApplicationContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
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
