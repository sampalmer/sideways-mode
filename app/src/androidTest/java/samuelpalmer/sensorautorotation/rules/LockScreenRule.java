package samuelpalmer.sensorautorotation.rules;

import android.app.Activity;
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

import java.util.concurrent.CountDownLatch;

import samuelpalmer.sensorautorotation.processes.ui.DismissKeyguardFragment;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;

/**
 * Provides the abilities to toggle whether the device has a secure lock screen and also lock and unlock the device.
 */
public abstract class LockScreenRule extends ExternalResource {

    static final String PIN = "1234";

    public static LockScreenRule create(ActivityScenarioRule<? extends Activity> activity) {
        if (Build.VERSION.SDK_INT < 26)
            return new DeviceAdminLockScreenRule(activity);
        else
            return new ShellLockScreenRule();
    }

    UiDevice device;
    Instrumentation instrumentation;
    private KeyguardManager keyguardManager;
    private boolean deviceWasOriginallySecure;

    @Override
    protected void before() throws Throwable {
        super.before();

        instrumentation = InstrumentationRegistry.getInstrumentation();
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

    /**
     * Configures the device to use a secure lock screen
     */
    public void enableLockScreen() {
        if (deviceWasOriginallySecure)
            throw new IllegalStateException("Device is protected by a secure lock. Cannot make changes to lock screen.");

        enableLockScreenUnchecked();
    }

    /**
     * Removes any secure lock settings from the device. If the device is currently locked, then
     * the keyguard will <b>not</b> be dismissed.
     */
    public void disableLockScreen() {
        if (deviceWasOriginallySecure)
            throw new IllegalStateException("Device is protected by a secure lock. Cannot make changes to lock screen.");

        disableLockScreenUnchecked();

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

    abstract void enableLockScreenUnchecked();
    abstract void disableLockScreenUnchecked();

    /**
     * Brings the device to the lock screen with the keyguard displayed
     */
    public void lockDevice() {
        // Using the power button to lock the device since this simulates what the user would do
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
     * Dismisses the keyguard
     *
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

}
