package samuelpalmer.sensorautorotation.rules;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiObject2;

import java.io.IOException;

import samuelpalmer.sensorautorotation.processes.ui.testing.TestingDeviceAdmin;

/**
 * Uses Android's Device Administration API to control the secure screen lock
 */
class DeviceAdminLockScreenRule extends LockScreenRule {

    private final ActivityScenarioRule<? extends Activity> activity;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private String adminComponentString;

    public DeviceAdminLockScreenRule(ActivityScenarioRule<? extends Activity> activity) {
        this.activity = activity;
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

                UiObject2 activateButton = device.findObject(By.res("com.android.settings", "action_button"));
                if (activateButton == null)
                    throw new RuntimeException("Couldn't find activate button");

                activateButton.click();
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

}
