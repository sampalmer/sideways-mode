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

import java.util.Objects;

import samuelpalmer.sensorautorotation.processes.ui.testing.TestActivity;
import samuelpalmer.sensorautorotation.rules.ApplicationCommandRule;
import samuelpalmer.sensorautorotation.rules.GrantOverlayPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantSystemSettingPermissionRule;
import samuelpalmer.sensorautorotation.rules.GrantUsageStatsPermissionRule;
import samuelpalmer.sensorautorotation.rules.IncompatibleAppsRule;
import samuelpalmer.sensorautorotation.rules.MockDeviceOrientationRule;
import samuelpalmer.sensorautorotation.rules.ScreenRotationRule;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.Orientation;

public class IncompatibleApps {

    /** We need this activity open under the dashboard to ensure all screen orientations are allowed */
    @Rule
    public ActivityScenarioRule<TestActivity> testActivity = new ActivityScenarioRule<>(TestActivity.class);

    @Rule
    public GrantSystemSettingPermissionRule systemSettingPermission = new GrantSystemSettingPermissionRule();

    @Rule
    public MockDeviceOrientationRule device = new MockDeviceOrientationRule();

    @Rule
    public ApplicationCommandRule app = new ApplicationCommandRule();

    @Rule
    public ScreenRotationRule screen = new ScreenRotationRule();

    @Rule
    public IncompatibleAppsRule incompatibleApps = new IncompatibleAppsRule();

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
        incompatibleApps.markAsIncompatible(launcherPackage);
        check(false);
    }

    @Test
    public void overridesAppInList() {
        String testActivityPackage = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();
        incompatibleApps.markAsIncompatible(testActivityPackage);
        check(true);
    }

    private void check(boolean shouldOverride) {
        // Setting things up so the app tries to make the screen upside-down. The OS generally
        // prevents apps from turning the screen upside-down, so this is a good test to see whether
        // we're overriding the screen orientation.
        device.setRotation(Orientation.LEFT_SIDE);
        app.run(ApplicationCommand.ACTION_RIGHT_SIDE);
        Orientation screenRotation = screen.waitForRotation(shouldOverride ? Orientation.UPSIDE_DOWN : null);

        Assert.assertThat(screenRotation, shouldOverride ? CoreMatchers.is(Orientation.UPSIDE_DOWN) : CoreMatchers.not(Orientation.UPSIDE_DOWN));
    }

    private static String getLauncherPackage() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        PackageManager packageManager = context.getPackageManager();
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo launcher = packageManager.resolveActivity(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return Objects.requireNonNull(launcher).activityInfo.packageName;
    }

}
