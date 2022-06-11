package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import samuelpalmer.sensorautorotation.components.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationRule;
import samuelpalmer.sensorautorotation.rules.ApplicationSettingsRule;
import samuelpalmer.sensorautorotation.rules.GrantOverlayPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantUsageStatsPermissionRule;
import samuelpalmer.sensorautorotation.rules.MockDeviceOrientationRule;
import samuelpalmer.sensorautorotation.rules.ScreenRotationRule;
import samuelpalmer.sensorautorotation.rules.lockscreen.LockScreenRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Tests related to the incompatible apps feature
 */
public class IncompatibleApps {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    private final ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);
    private final LockScreenRule lockScreen = LockScreenRule.create(testActivity);

    @Rule
    public RuleChain activityAndLockScreen = RuleChain.outerRule(testActivity).around(lockScreen);

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public MockDeviceOrientationRule device = new MockDeviceOrientationRule();

    @Rule
    public ApplicationRule app = new ApplicationRule();

    @Rule
    public ScreenRotationRule screen = new ScreenRotationRule();

    @Rule
    public ApplicationSettingsRule appSettings = new ApplicationSettingsRule();

    @Rule
    public GrantUsageStatsPermissionRule usagePermission = new GrantUsageStatsPermissionRule();

    @Rule
    public GrantOverlayPermissionRule drawOverAppsPermission = new GrantOverlayPermissionRule();

    @Before
    public void makeAppIncompatible() {
        testActivity.getScenario().onActivity(a -> a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR));
    }

    @Test
    public void doesntOverrideAppNotInList() {
        check(false);
    }

    @Test
    public void doesntOverrideWhenOtherAppInList() {
        String launcherPackage = getLauncherPackage();
        appSettings.markAsIncompatible(launcherPackage);
        check(false);
    }

    @Test
    public void overridesAppInList() {
        String testActivityPackage = getTestActivityPackage();
        appSettings.markAsIncompatible(testActivityPackage);
        check(true);
    }

    @Test
    public void doesntOverrideLockScreen() {
        // The lock screen isn't an activity, so when the device is locked, our app will still see the current activity as being whatever is behind the lock screen
        String testActivityPackage = getTestActivityPackage();
        appSettings.markAsIncompatible(testActivityPackage);

        // We need the app to stay running when the device is locked in order to test this
        appSettings.setTurnOffWhenLocked(false);

        lockScreen.enableLockScreen();
        lockScreen.lockDevice();

        check(false);
    }

    private void check(boolean shouldOverride) {
        // Setting things up so the app tries to make the screen upside-down. The OS generally
        // prevents apps from turning the screen upside-down, so this is a good test to see whether
        // we're overriding the screen orientation.
        device.setRotation(Orientation.LEFT_SIDE);
        app.startApp(ApplicationCommand.ACTION_RIGHT_SIDE);
        Orientation screenRotation = screen.waitForScreenToSettle();

        Assert.assertThat(screenRotation, shouldOverride ? CoreMatchers.is(Orientation.UPSIDE_DOWN) : CoreMatchers.not(Orientation.UPSIDE_DOWN));
    }

    private static String getLauncherPackage() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo launcher = packageManager.resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return Objects.requireNonNull(launcher).activityInfo.packageName;
    }

    private String getTestActivityPackage() {
        AtomicReference<String> resultHolder = new AtomicReference<>();
        testActivity.getScenario().onActivity(a -> resultHolder.set(a.getPackageName()));
        String result = resultHolder.get();
        if (result == null)
            throw new RuntimeException("Couldn't get test activity package");

        return result;
    }

}
