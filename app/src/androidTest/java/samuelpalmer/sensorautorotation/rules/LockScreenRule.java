package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import samuelpalmer.sensorautorotation.processes.ui.DismissKeyguardFragment;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;

public class LockScreenRule extends ExternalResource {

    private static final String PIN = "1234";

    private UiDevice device;
    private KeyguardManager keyguardManager;
    private boolean deviceWasOriginallySecure;

    @Override
    protected void before() throws Throwable {
        super.before();

        if (Build.VERSION.SDK_INT < 26)
            // TODO: Add support for older device versions using Device Admin or similar
            throw new UnsupportedOperationException("Lock screen can only be changed on Android 8+");

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        Context targetContext = instrumentation.getTargetContext();
        keyguardManager = ContextCompat.getSystemService(targetContext, KeyguardManager.class);
        assert keyguardManager != null;

        deviceWasOriginallySecure = keyguardManager.isKeyguardSecure();
    }

    @Override
    protected void after() {
        super.after();

        if (!deviceWasOriginallySecure) {
            // We must remove the lock screen after each test, even on test failure, so we don't break a subsequent test and lock the user out of the device
            disableLockScreen();
        }
    }

    /**
     * Determines whether the device was configured with a secure lock screen before tests were run.
     * If this is <c>true</c>, then the we cannot make changes to the lock screen.
     */
    public boolean wasDeviceOriginallySecure() {
        return deviceWasOriginallySecure;
    }

    public void enableLockScreen() {
        if (deviceWasOriginallySecure)
            throw new IllegalStateException("Device is protected by a secure lock. Cannot make changes to lock screen.");

        runCommand("set-pin %s", PIN);
    }

    /**
     * Removes any secure lock settings from the device. If the device is currently locked, then
     * the keyguard will <b>not</b> be dismissed.
     */
    public void disableLockScreen() {
        if (deviceWasOriginallySecure)
            throw new IllegalStateException("Device is protected by a secure lock. Cannot make changes to lock screen.");

        runCommand("clear --old %s", PIN);

        boolean isLocked = LockScreenMonitor.poll(keyguardManager);
        if (!isLocked)
            return;

        // Turning the screen off and on so the secure keyguard converts into a swipe keyguard
        try {
            device.sleep();
            Thread.sleep(500); // We need to wait between presses so the device doesn't think this is the power button double-tap shortcut to open the camera
            device.wakeUp();
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void lockDevice() {
        // TODO: Use device admin to lock the device since that should be faster and more reliable

        try {
            device.sleep(); // Pressing power button to turn off screen like a user would
            Thread.sleep(500); // We need to wait between presses so the device doesn't think this is the power button double-tap shortcut to open the camera
            device.wakeUp(); // Pressing power button to turn on screen like a user would
        } catch (RemoteException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // We'd better make sure we're actually on the lock screen so we know the test is working
        boolean isLocked = LockScreenMonitor.poll(keyguardManager);
        if (!isLocked)
            throw new RuntimeException("Couldn't lock the device. Is the device's \"Power button instantly locks\" setting off?");
    }

    /**
     * @param activityScenarioRule An activity that is currently open. This will be used to dismiss the keyguard.
     * @throws IllegalStateException If the keyguard is secure
     */
    public void unlockDevice(ActivityScenarioRule<? extends AppCompatActivity> activityScenarioRule) {
        boolean isLocked = LockScreenMonitor.poll(keyguardManager);
        if (!isLocked)
            return;

        if (keyguardManager.isKeyguardSecure())
            throw new IllegalStateException("Cannot unlock device because the keyguard is secure");

        // Dismissing the keyguard, which unfortunately needs to be done using an activity and fragment
        CountDownLatch unlocked = new CountDownLatch(1);
        activityScenarioRule.getScenario().onActivity(a -> {
            FragmentManager fragmentManager = a.getSupportFragmentManager();
            DismissKeyguardFragment dismissKeyguardFragment = new DismissKeyguardFragment();

            fragmentManager
                    .beginTransaction()
                    .add(dismissKeyguardFragment, null)
                    .commitNowAllowingStateLoss();

            dismissKeyguardFragment.unlockAndRun(() -> {
                fragmentManager
                        .beginTransaction()
                        .remove(dismissKeyguardFragment)
                        .commitNowAllowingStateLoss();

                unlocked.countDown();
            });
        });

        try {
            unlocked.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void runCommand(String format, Object... args) {
        String command = "locksettings " + String.format(format, args);

        String response;
        try {
            response = device.executeShellCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response == null || response.equals(""))
            // Unfortunately, we don't get the exit code or error output, so the only way to detect an error is by checking standard output
            throw new RuntimeException("Unknown error when running shell command " + command);
    }

}
