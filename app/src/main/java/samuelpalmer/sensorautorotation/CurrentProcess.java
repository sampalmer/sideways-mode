package samuelpalmer.sensorautorotation;

import android.app.AppOpsManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Process;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.PersistentNotificationUpdater;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps.LoadInstalledAppsTask;
import samuelpalmer.sensorautorotation.upgrade.Upgrader;
import samuelpalmer.sensorautorotation.utilities.LoggingAppOpsCollector;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Performs process startup logic, tracks the application context, and identifies which process the calling code is running in.
 */
public class CurrentProcess extends Application {

    private static final String TAG = CurrentProcess.class.getSimpleName();

    // Whether to audit usage of dangerous permissions. This generates a lot of log noise, so it's off by default.
    private static final boolean AUDIT_PERMISSIONS = false;

    @Nullable
    private static Boolean isServiceProcess;

    // Holding a reference to this just to make sure nothing gets garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private PersistentNotificationUpdater persistentNotificationUpdater;

    public static boolean isServiceProcess(Context context) {
        if (isServiceProcess == null) {
            String processName = CurrentProcessNameDetector.getName();

            String packageName = context.getPackageName();
            if (processName.equals(packageName + context.getText(R.string.process_service)))
                isServiceProcess = true;
            else if (processName.equals(packageName + context.getText(R.string.process_ui)))
                isServiceProcess = false;
            else
                throw new RuntimeException("Unrecognised process name: " + processName);
        }

        return isServiceProcess;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Application.onCreate() is called *after* ContentProvider.onCreate().
        // However, attachBaseContext() is called before it, which is why this method is used here.
        Log.i(TAG, "Process created");

        /*
         * Sometimes Android makes two instances of Application and its components within the one process.
         * When this happens, the whole process is effectively corrupt and unusable, so we'll just kill it
         * so it doesn't crash and generate an error report.
         *
         * https://issuetracker.google.com/issues/36972466#comment14
         */
        if (getResources() == null) {
            Log.w(TAG, "Process is in a bad state. Killing process.");
            Process.killProcess(Process.myPid());
        }
    }
    
    /**
     * This is effectively the entrypoint into the current process.
     * However, this is still called after {@link ContentProvider#onCreate()}.
     */
    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .permitDiskReads() // So we can read SharedPreferences and the current process name
                .permitDiskWrites() // So SharedPreferences can initialise itself on first-run
                .penaltyLog()
                // Not using penaltyDialog since it's too annoying
                .build()
            );

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                // Not using penaltyDeath since it makes it look like the app is malfunctioning
                .build()
            );

            if (AUDIT_PERMISSIONS) {
                if (Build.VERSION.SDK_INT == 29 && Build.VERSION.PREVIEW_SDK_INT >= 1) {
                    // Logging all uses of dangerous app ops to make sure no privacy-breaching code makes it to production
                    AppOpsManager appOpsManager = getSystemService(AppOpsManager.class);
                    assert appOpsManager != null;
                    appOpsManager.setNotedAppOpsCollector(new LoggingAppOpsCollector());
                }
            }
        }

        super.onCreate();

        // Note that crashes that occur here don't appear to prevent the rest of the application from running, at least on Android 7.1 (not sure about other OS versions)
        
        /*
         * Must setup notification channels before doing anything else so channels will exist when notifications are posted.
         * Doing this regardless of which process we're in since notifications can (technically) be posted from either process.
         */
        if (Build.VERSION.SDK_INT >= 26)
            NotificationChannels.setup(this);

        // Must upgrade settings before any settings-related code runs.
        Upgrader.upgradeIfNecessary(this);
        
        if (!isServiceProcess(this)) {
            SharedPreferences applicationSettings = ApplicationSettings.getSharedPreferences(this);
            
            /*
              Automatically pushes changes to user preferences from the UI process to the service process.
              This ensures preference changes made by the user appear to instantly take effect.
             */
            applicationSettings.registerOnSharedPreferenceChangeListener(applicationSettingsChanged);
            
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
            registerReceiver(appMaybeUninstalledReceiver, appMaybeUninstalled);
            
            /*
                There might be some apps in the incompatible apps list that are not installed on this device.
                For example:
                * The user has done a config restore onto a different device, so a different set of apps is installed
                * This app has just been re-installed and is using a backed-up config from the last time it was installed
             */
            removeUninstalledIncompatibleApps(applicationSettings);
            
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        } else {
            persistentNotificationUpdater = new PersistentNotificationUpdater(this);
            persistentNotificationUpdater.ensureStarted();
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // This isn't called on real devices, so there's no need to do cleanup work here.
        
        Log.i(TAG, "Process terminated");
    }
    
    // Do not re-use. This is just stored in a field to prevent garbage collection of the listener.
    private final SharedPreferences.OnSharedPreferenceChangeListener applicationSettingsChanged = (sharedPreferences, s) -> {
        if (ApplicationCommand.Config.isSyncableAppSetting(s)) {
            Log.i(TAG, "User preferences changed. Sending to service process.");

            new ApplicationCommand(
                ApplicationCommand.ACTION_SYNC_CONFIG,
                ApplicationSettings.getSharedPreferences(CurrentProcess.this)
            ).send(CurrentProcess.this);
        }
    };
    
    private final BroadcastReceiver appMaybeUninstalledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isReplacement = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            if (isReplacement)
                return;
            
            SharedPreferences applicationSettings = ApplicationSettings.getSharedPreferences(CurrentProcess.this);
            removeUninstalledIncompatibleApps(applicationSettings);
        }
    };
    
    private void removeUninstalledIncompatibleApps(SharedPreferences applicationSettings) {
        Set<String> installedPackageNames = LoadInstalledAppsTask.getInstalledPackages(getPackageManager(), null);
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
