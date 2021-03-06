package samuelpalmer.sensorautorotation.rules.lockscreen;

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

import org.junit.AssumptionViolatedException;
import org.junit.rules.ExternalResource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import samuelpalmer.sensorautorotation.processes.ui.DismissKeyguardFragment;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;

/**
 * Provides the abilities to toggle whether the device has a secure lock screen and also lock and unlock the device.
 */
public abstract class LockScreenRule extends ExternalResource {

    static final String PIN = "1234";

    LockScreenRule(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        this.activity = activity;
    }

    public static LockScreenRule create(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        if (Build.VERSION.SDK_INT < 26)
            return new DeviceAdminLockScreenRule(activity);
        else
            return new ShellLockScreenRule(activity);
    }

    final ActivityScenarioRule<? extends AppCompatActivity> activity;
    UiDevice device;
    Instrumentation instrumentation;
    private KeyguardManager keyguardManager;

    /**
     * Determines whether the device was configured with a secure lock screen before tests were run.
     * If this is <c>true</c>, then the we cannot make changes to the lock screen.
     */
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
            unlockDevice(activity);
            disableLockScreen();
        }
    }

    /**
     * Configures the device to use a secure lock screen
     */
    public void enableLockScreen() {
        checkAccess();

        enableLockScreenUnchecked();

        if (!keyguardManager.isKeyguardSecure())
            throw new RuntimeException("Failed to enable lock screen");
    }

    /**
     * Removes any secure lock settings from the device. If the device is currently locked, then
     * the keyguard will <b>not</b> be dismissed.
     *
     * @throws IllegalStateException if device is currently locked
     */
    @SuppressWarnings("WeakerAccess")
    public void disableLockScreen() {
        // Check if it's already disabled
        boolean isSecure = keyguardManager.isKeyguardSecure();
        if (!isSecure)
            return;

        checkAccess();

        // Make sure it's unlocked since we cannot disable the lock screen while locked on Android 5.0
        boolean isLocked = LockScreenMonitor.poll(keyguardManager);
        if (isLocked)
            throw new IllegalStateException("Cannot disable lock screen while device is locked");

        disableLockScreenUnchecked();

        if (keyguardManager.isKeyguardSecure())
            throw new RuntimeException("Failed to disable lock screen");
    }

    abstract void enableLockScreenUnchecked();
    abstract void disableLockScreenUnchecked();

    /**
     * Brings the device to the lock screen with the keyguard displayed
     */
    public void lockDevice() {
        // Don't want to lock the device if we won't be able to unlock it again
        checkAccess();

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
     * Dismisses the keyguard. If a lock is currently set-up, then it will be unlocked.
     *
     * @param activityScenarioRule An activity that is currently open. This will be used to dismiss the keyguard.
     */
    @SuppressWarnings("WeakerAccess")
    public void unlockDevice(ActivityScenarioRule<? extends AppCompatActivity> activityScenarioRule) {
        boolean isLocked = LockScreenMonitor.poll(keyguardManager);
        if (!isLocked)
            return;

        boolean isSecure = keyguardManager.isKeyguardSecure();

        // Dismissing the keyguard, which unfortunately needs to be done using an activity and fragment
        CountDownLatch showingUnlockScreen = new CountDownLatch(1);
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

            showingUnlockScreen.countDown();
        });

        try {
            if (!showingUnlockScreen.await(1, TimeUnit.SECONDS))
                throw new RuntimeException("Took too long to show unlock screen");
            Thread.sleep(500); // Waiting for the keyguard to animate in so it can accept input

            if (isSecure) {
                // Bailing out if we know we won't be able to unlock the device, since we don't want to waste time or lock the user out
                checkAccess();

                LockScreenPinEnterer.enterPin(device, PIN);
            }

            if (!unlocked.await(1, TimeUnit.SECONDS))
                throw new RuntimeException("Took too long to unlock device");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkAccess() {
        if (deviceWasOriginallySecure)
            throw new AssumptionViolatedException("Device is protected by a secure lock. Cannot use or change the lock screen.");
    }

}
