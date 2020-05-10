package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.PersistentNotificationUpdater;
import samuelpalmer.sensorautorotation.processes.ui.AppCompatibilityChecker;
import samuelpalmer.sensorautorotation.processes.ui.AppSettingsSynchroniser;
import samuelpalmer.sensorautorotation.processes.ui.AppUninstalledUpdater;
import samuelpalmer.sensorautorotation.upgrade.Upgrader;

/**
 * Performs process startup logic, tracks the application context, and identifies which process the calling code is running in.
 */
public class CurrentProcess extends Application {

    private static final String TAG = CurrentProcess.class.getSimpleName();
    
    @Nullable
    private static Boolean isServiceProcess;

    // Holding a reference to this just to make sure nothing gets garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private PersistentNotificationUpdater persistentNotificationUpdater;

    // Holding a reference to this just to make sure nothing gets garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private AppUninstalledUpdater appUninstalledUpdater;

    // Holding a reference to this just to make sure nothing gets garbage-collected
    @SuppressWarnings("FieldCanBeLocal")
    private AppSettingsSynchroniser appSettingsSynchroniser;

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
        if (BuildConfig.DEBUG)
            DebugConfig.configureDebug();

        super.onCreate();

        // Note that crashes that occur here don't appear to prevent the rest of the application from running, at least on Android 7.1 (not sure about other OS versions)
        
        /*
         * Must setup notification channels before doing anything else so channels will exist when notifications are posted.
         * Doing this regardless of which process we're in since notifications can (technically) be posted from either process.
         * We cannot rely on the app settings to determine whether this is needed since the app settings could have been
         * migrated from a different device.
         */
        if (Build.VERSION.SDK_INT >= 26)
            NotificationChannels.setup(this);

        // Must upgrade settings before any settings-related code runs.
        Upgrader.upgradeIfNecessary(this);

        if (isServiceProcess(this)) {
            persistentNotificationUpdater = new PersistentNotificationUpdater(this);
            persistentNotificationUpdater.ensureStarted();
        } else {
            appSettingsSynchroniser = new AppSettingsSynchroniser(this);
            appSettingsSynchroniser.ensureStarted();

            appUninstalledUpdater = new AppUninstalledUpdater(this);
            appUninstalledUpdater.ensureStarted();

            registerActivityLifecycleCallbacks(new AppCompatibilityChecker());

            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // This isn't called on real devices, so there's no need to do cleanup work here.
        
        Log.i(TAG, "Process terminated");
    }

}
