package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps.LoadInstalledAppsTask;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Automatically removes apps from the {@link ApplicationSettings#KEY_INCOMPATIBLE_APP_PACKAGES}
 * setting when they are uninstalled.
 *
 * In general, when an app is uninstalled and re-installed, then the user expects its behaviour to
 * revert to when it was first installed. By removing the app from {@link ApplicationSettings#KEY_INCOMPATIBLE_APP_PACKAGES},
 * this gives the user that same experience.
 *
 * In addition, this is important for the user's privacy. We only have knowledge of their installed
 * apps when absolutely necessary.
 */
public class AppUninstalledUpdater extends State {

    private static final String TAG = AppUninstalledUpdater.class.getSimpleName();

    private final Context context;
    private final SharedPreferences applicationSettings;

    public AppUninstalledUpdater(Context context) {
        this.context = context;
        applicationSettings = ApplicationSettings.getSharedPreferences(context);
    }

    @Override
    protected void start() {
        /*
            We need to remove apps from the incompatible apps list if they are disabled or uninstalled.

            Ideally, we would register broadcast receivers in the manifest so we get notified even when
            the UI process isn't running. However, Android O's background execution limits
            prevent us from picking up some relevant broadcasts. So we'll just have to make
            do with what we've got.

            See https://developer.android.com/about/versions/oreo/background.html#broadcasts.
         */
        IntentFilter appMaybeUninstalled = new IntentFilter();
        appMaybeUninstalled.addAction(Intent.ACTION_PACKAGE_CHANGED);
        appMaybeUninstalled.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appMaybeUninstalled.addDataScheme("package");
        context.registerReceiver(appMaybeUninstalledReceiver, appMaybeUninstalled);

        /*
            There might be some apps in the incompatible apps list that are not installed on this device.
            For example:
            * The user has done a config restore onto a different device, so a different set of apps is installed
            * This app has just been re-installed and is using a backed-up config from the last time it was installed
         */
        removeUninstalledIncompatibleApps(applicationSettings, null);
    }

    @Override
    protected void stop() {
        context.unregisterReceiver(appMaybeUninstalledReceiver);
    }

    private final BroadcastReceiver appMaybeUninstalledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Don't want to lose config if the app is just being upgraded
            boolean isReplacement = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (isReplacement)
                return;

            String packageName = null;
            Uri data = intent.getData();
            if (data != null && "package".equals(data.getScheme()))
                packageName = data.getSchemeSpecificPart();

            if (packageName == null)
                Log.w(TAG, "Couldn't determine package for intent: " + intent);

            removeUninstalledIncompatibleApps(applicationSettings, packageName);
        }
    };

    private void removeUninstalledIncompatibleApps(SharedPreferences applicationSettings, @Nullable String packageToCheck) {
        Set<String> installedPackagesToCheck = LoadInstalledAppsTask.getInstalledPackages(context.getPackageManager(), packageToCheck);
        Set<String> incompatibleAppPackages = applicationSettings.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());

        Set<String> toRemove = new HashSet<>();
        for (String incompatibleAppPackage : incompatibleAppPackages)
            // Only considering removal of packageToCheck
            if (packageToCheck == null || incompatibleAppPackage.equals(packageToCheck))
                if (!installedPackagesToCheck.contains(incompatibleAppPackage))
                    toRemove.add(incompatibleAppPackage);

        if (!toRemove.isEmpty()) {
            Log.i(TAG, "Some incompatible apps have been uninstalled or disabled. Removing from the list.");
            Set<String> newIncompatibleAppPackages = new HashSet<>(incompatibleAppPackages); // Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
            newIncompatibleAppPackages.removeAll(toRemove);
            applicationSettings.edit().putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, newIncompatibleAppPackages).apply();
        }
    }

}
