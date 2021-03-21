package samuelpalmer.sensorautorotation;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import androidx.core.content.ContextCompat;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeoutException;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.UpgradeReceiver;
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class WhenUpgraded {

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Test
    public void serviceIsRestartedInSameState() {
        ApplicationStatus originalState = app.startApp(ApplicationCommand.ACTION_LEFT_SIDE);

        simulateAppUpgrade();

        ApplicationStatus newState;
        try {
            newState = app.waitForAppToStart();
        } catch (TimeoutException e) {
            Assert.fail("App did not start after being upgraded");
            return;
        }

        Assert.assertThat("App state was different after restarting from the upgrade", newState, CoreMatchers.equalTo(originalState));
    }

    private void simulateAppUpgrade() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context targetContext = instrumentation.getTargetContext();

        // Prevent service process from automatically restarting
        Intent intent = new Intent(targetContext, RotationService.class);
        intent.putExtra(RotationService.EXTRA_NOT_STICKY, true);
        if (targetContext.startService(intent) == null)
            throw new RuntimeException("Failed to set service to not sticky");

        // Wait for service to receive above command
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Kill service process
        int serviceProcessId = getServiceProcessId(targetContext);
        Process.killProcess(serviceProcessId);

        // Ensure it doesn't automatically restart
        try {
            app.waitForAppToStart();
            throw new RuntimeException("App automatically restarted after service process killed");
        } catch (TimeoutException ignored) {
            // The app didn't automatically restart, so we're ok to proceed
        }

        // Tell app it was upgraded
        // Not sending ACTION_MY_PACKAGE_REPLACED because we don't have permission, even through
        // ADB on higher OS versions
        targetContext.sendBroadcast(new Intent(targetContext, UpgradeReceiver.class));
    }

    private static int getServiceProcessId(Context targetContext) {
        String serviceProcessName = targetContext.getPackageName() + targetContext.getText(R.string.process_service);

        ActivityManager activityManager = ContextCompat.getSystemService(targetContext, ActivityManager.class);
        assert activityManager != null;
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo process : processes)
            if (process.processName.equals(serviceProcessName))
                return process.pid;

        throw new IllegalStateException("Service process is not running");
    }

}
