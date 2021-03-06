package samuelpalmer.sensorautorotation.rules.lockscreen;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import samuelpalmer.sensorautorotation.components.TestingDeviceAdmin;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Uses Android's Device Administration API to control the secure screen lock
 */
class DeviceAdminLockScreenRule extends LockScreenRule {

    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private String adminComponentString;

    public DeviceAdminLockScreenRule(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        super(activity);
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        Context targetContext = instrumentation.getTargetContext();
        devicePolicyManager = ContextCompat.getSystemService(targetContext, DevicePolicyManager.class);
        assert devicePolicyManager != null;

        adminComponent = new ComponentName(targetContext, TestingDeviceAdmin.class);
        adminComponentString = adminComponent.flattenToString();

        if (Build.VERSION.SDK_INT >= 24) {
            // Using profile owner instead of regular "active admin" since only profile owners can clear passwords on Android N
            if (!devicePolicyManager.isProfileOwnerApp(adminComponent.getPackageName()))
                // We can't set ourselves as profile owner if we, or someone else, are already profile owner
                runCommand("dpm set-profile-owner %s", this.adminComponentString);
        } else if (Build.VERSION.SDK_INT >= 22)
            // Android 5.1 and above let us set device admin from the shell
            runCommand("dpm set-active-admin %s", this.adminComponentString);
        else {
            // Unfortunately, UI automation is the only way on older OS versions!
            if (!devicePolicyManager.isAdminActive(adminComponent)) { // The Activate Admin UI only opens if we're not already admin
                Intent addAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);

                // The OS prevents us from showing the device admin activity unless we get there from another activity
                activity.getScenario().onActivity(a -> a.startActivity(addAdminIntent));
                if (!device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 5000))
                    throw new RuntimeException("Took too long for add device admin activity to start");

                // Get activate button
                UiObject2 activateButton = device.wait(Until.findObject(By.res("com.android.settings", "action_button")), 2000);
                if (activateButton == null)
                    throw new RuntimeException("Couldn't find activate button");

                activateButton.click();

                // We don't know how long it will take for the activation to occur, so we'll wait
                waitForAdminActivation();
            }
        }

        if (!devicePolicyManager.isAdminActive(adminComponent))
            throw new RuntimeException("Admin is not active");
    }

    @Override
    protected void after() {
        super.after();

        if (Build.VERSION.SDK_INT >= 24)
            // We need to use the shell to remove *profile* admin since the API only removes regular admins
            runCommand("dpm remove-active-admin %s", adminComponentString);
        else
            devicePolicyManager.removeActiveAdmin(adminComponent);
    }

    @Override
    void enableLockScreenUnchecked() {
        boolean success = devicePolicyManager.resetPassword(PIN, 0);
        if (!success)
            throw new RuntimeException("Failed to set device password");
    }

    @Override
    void disableLockScreenUnchecked() {
        // Must pass in a blank string to clear the password. Cannot pass in null because it crashes on Android 5.0.
        boolean success = devicePolicyManager.resetPassword("", 0);
        if (!success)
            throw new RuntimeException("Failed to clear device password");

        if (Build.VERSION.SDK_INT < 22) {
            // Older OS versions don't disable the lock screen after you clear the password, so we need to use UI Automation to do this
            activity.getScenario().onActivity(a -> a.startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD)));
            if (!device.wait(Until.hasObject(By.pkg("com.android.settings").depth(0)), 5000))
                throw new RuntimeException("Took too long for set PIN activity to start");

            UiObject2 screenLockTypes = device.findObject(By.res("android", "list"));
            UiObject2 noneButton = screenLockTypes.getChildren().get(0);

            noneButton.click();

            // Waiting for the change to take effect after clicking the button
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void runCommand(String format, Object... args) {
        String command = String.format(format, args);

        String result;
        try {
            result = device.executeShellCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (result == null || result.equals(""))
            throw new RuntimeException("Failed to run command " + command);
    }

    private static void waitForAdminActivation() {
        try {
            // Broadcasts and events occur in the main thread, so we'll have to listen over there
            Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

            CountDownLatch activated = new CountDownLatch(1);
            EventHandler adminActivated = activated::countDown;

            // Subscribe to activation event
            CountDownLatch subscribed = new CountDownLatch(1);
            mainHandler.post(() -> {
                TestingDeviceAdmin.onEnabled.subscribe(adminActivated);
                subscribed.countDown();

                // The connection might have occurred before we got here
                if (TestingDeviceAdmin.isEnabled())
                    activated.countDown();
            });
            if (!subscribed.await(5000, TimeUnit.MILLISECONDS))
                throw new RuntimeException("Took too long for subscribe to device admin activation event");

            // Wait for activation
            try {
                if (!activated.await(2, TimeUnit.SECONDS))
                    throw new RuntimeException("Took too long for device admin to activate");
            } finally {
                // Unsubscribe from activation event
                CountDownLatch unsubscribed = new CountDownLatch(1);
                mainHandler.post(() -> {
                    TestingDeviceAdmin.onEnabled.unsubscribe(adminActivated);
                    unsubscribed.countDown();
                });
                if (!unsubscribed.await(5000, TimeUnit.MILLISECONDS))
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException("Took too long to unsubscribe from activation event");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
