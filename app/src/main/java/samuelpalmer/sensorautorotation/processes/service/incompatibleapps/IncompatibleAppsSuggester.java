package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

import java.util.Collections;
import java.util.Set;

public class IncompatibleAppsSuggester extends State {
    
    private final Context context;
    private final ActivityMonitor activityMonitor;
    private final SharedPreferences preferences;
    private final OverriddenOrientationDetector overriddenOrientationDetector;
    private final SupportedOrientationsDetector supportedOrientationsDetector;
    private final NotificationManager notificationManager;
    private final PackageManager packageManager;

    private Set<String> incompatibleAppPackages;

    public IncompatibleAppsSuggester(Context context, ActivityMonitor activityMonitor, ScreenRotationMonitor screenRotationMonitor, LockScreenMonitor lockScreenMonitor) {
        this.context = context;
        this.activityMonitor = activityMonitor;
        preferences = ServiceSettings.getSharedPreferences(context);
        overriddenOrientationDetector = new OverriddenOrientationDetector(context, screenRotationMonitor, lockScreenMonitor);
        supportedOrientationsDetector = new SupportedOrientationsDetector(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        packageManager = context.getPackageManager();
    }
    
    @Override
    protected void start() {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        activityMonitor.subscribe(activityChanged);
        overriddenOrientationDetector.subscribe(anythingChanged);
        supportedOrientationsDetector.subscribe(anythingChanged);
        
        loadIncompatibleAppPackages();
        
        check();
    }
    
    @Override
    protected void stop() {
        supportedOrientationsDetector.unsubscribe(anythingChanged);
        overriddenOrientationDetector.unsubscribe(anythingChanged);
        activityMonitor.unsubscribe(activityChanged);
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
    
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (isStarted() && key.equals(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES)) {
            loadIncompatibleAppPackages();
            check();
            cancelObsoleteNotifications();
        }
    };
    
    private void loadIncompatibleAppPackages() {
        incompatibleAppPackages = preferences.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet());
    }
    
    private final EventHandler activityChanged = new EventHandler() {
        @Override
        public void update() {
            overriddenOrientationDetector.reset();
            supportedOrientationsDetector.reset();
            check();
        }
    };
    
    private final EventHandler anythingChanged = this::check;
    
    private void check() {
        ComponentName currentActivity = activityMonitor.currentOrNull();

        if (
            currentActivity != null // We can't do anything if we don't know which activity is open
            &&
            !incompatibleAppPackages.contains(currentActivity.getPackageName()) // No point prompting if the app is already marked as incompatible
            &&
            overriddenOrientationDetector.overridesOrientation()
            &&
            supportedOrientationsDetector.supportsLandscape() && supportedOrientationsDetector.supportsPortrait() // Apps that are locked in the one orientation aren't a problem
            &&
            !currentActivity.getPackageName().equals(context.getPackageName()) // Sideways Mode might looks like it's incompatible if it's open over the top of an incompatible app
            // TODO: Don't prompt if a notification already exists for this app
            // TODO: Don't prompt if the user has asked not to be prompted again for this app
            // TODO: Don't prompt if the user has already been asked and dismissed the notification
            // TODO: Don't prompt if the current activity orientation is controlled by the activity behind it (eg a dialog)
            // TODO: For some reason a suggestion was made to mark the launcher as incompatible when I opened YouTube. This might be a timing-related issue caused by the accessibility service. If that's the case, then the issue no longer applies since the accessibility service has been replaced with polling.
        ) {
            try {
                ActivityInfo activityInfo = packageManager.getActivityInfo(currentActivity, 0);
                if (!isFloating(activityInfo)) {
                    // TODO: We might have lost track of what the real current activity is. For example, the user might have opened the recents screen and returned to an app, but we might have only been notified about the recents screen.
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(currentActivity.getPackageName(), 0);
                    Intent intent = MarkAppAsIncompatibleReceiver.makeIntent(context, currentActivity.getPackageName());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, PendingIntentIds.INCOMPATIBLE_APPS_SUGGESTION, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
                    String summary = "Is " + applicationInfo.loadLabel(packageManager) + " incompatible?";

                    // TODO: Stack/group multiple of these notifications so you get one for each incompatible app?
                    NotificationBuilderCompat builder = NotificationBuilderFactory
                            .create(context)
                            .setSmallIcon(R.drawable.notification_rotation)
                            .setContentTitle(summary)
                            .setContentText("Tap to fix")
                            .setContentIntent(pendingIntent)
                            .setChannelId(NotificationChannels.ID_INCOMPATIBLE)
//                            .setDeleteIntent() // TODO: Don't ask again if user dismisses it?
                            .setTicker(summary) // Helps ensure the user actually notices notification. Only works on Android 4.
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
//                            .addAction() // TODO: Provide button to not ask again
                            ;

                    // TODO: Add a sound and maybe a vibration since the user might be in the middle of a full screen video or something

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
                        // Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
                        // the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
                        builder.setVibrate(new long[]{});

                    notificationManager.notify(currentActivity.getPackageName(), NotificationIds.INCOMPATIBLE_APP, builder.build());
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(getClass().getSimpleName(), e); // Don't really need to worry about this; maybe the app got uninstalled or something
            }
        }
    }

    private void cancelObsoleteNotifications() {
        // The user might have added a suggested app to the incompatible apps list,
        // so we'd better cancel the corresponding notification.
        for (String incompatibleAppPackage : incompatibleAppPackages)
            notificationManager.cancel(incompatibleAppPackage, NotificationIds.INCOMPATIBLE_APP);
    }

    /**
     * Determines whether {@link android.R.attr#windowIsFloating} is set to {@code true} for the
     * activity. This is typically the case for dialogs, and it causes the OS to use the activity
     * behind it to determine screen orientation, depending on the activity's orientation setting.
     *
     * @throws PackageManager.NameNotFoundException when the current app has been uninstalled
     */
    private boolean isFloating(ActivityInfo activityInfo) throws PackageManager.NameNotFoundException {
        Resources resources = packageManager.getResourcesForApplication(activityInfo.packageName);
        Resources.Theme theme = resources.newTheme();
        int themeResource = activityInfo.getThemeResource();
        TypedArray typedArray = theme.obtainStyledAttributes(themeResource, new int[]{android.R.attr.windowIsFloating});

        try {
            return typedArray.getBoolean(0, false);
        }
        finally {
            typedArray.recycle();
        }
    }

}
