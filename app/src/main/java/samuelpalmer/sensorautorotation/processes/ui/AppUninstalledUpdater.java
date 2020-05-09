package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

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
        removeUninstalledIncompatibleApps(applicationSettings);
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

            removeUninstalledIncompatibleApps(applicationSettings);
        }
    };

    private void removeUninstalledIncompatibleApps(SharedPreferences applicationSettings) {
        Set<String> installedPackageNames = LoadInstalledAppsTask.getInstalledPackages(context.getPackageManager(), null);
        Set<String> incompatibleAppPackages = applicationSettings.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());

        Set<String> toRemove = new HashSet<>();
        for (String incompatibleAppPackage : incompatibleAppPackages)
            if (!installedPackageNames.contains(incompatibleAppPackage))
                toRemove.add(incompatibleAppPackage);

        if (!toRemove.isEmpty()) {
            Log.i(getClass().getSimpleName(), "Some incompatible apps are no longer installed. Removing from the list.");
            incompatibleAppPackages = new HashSet<>(incompatibleAppPackages); // Need to clone the set to prevent bug https://stackoverflow.com/q/9803838/238753.
            incompatibleAppPackages.removeAll(toRemove);
            applicationSettings.edit().putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages).apply();
        }
    }

}
