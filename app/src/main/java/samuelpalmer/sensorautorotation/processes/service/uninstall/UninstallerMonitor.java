package samuelpalmer.sensorautorotation.processes.service.uninstall;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.ContextCompat;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.processes.ui.UninstallTipActivity;
import samuelpalmer.sensorautorotation.utilities.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

import java.util.*;

/**
 * Detects when the user goes to uninstall an app, and then warns them that
 * they need to turn off Sideways Mode before uninstalling it.
 */
class UninstallerMonitor extends State {

    private final Context context;
    private final ActivityMonitor activityMonitor;
    private final PackageManager packageManager;
    private final NotificationManager notificationManager;

    private final List<Intent> uninstallerIntents;
    private final Set<ComponentName> uninstallerActivities;

    public UninstallerMonitor(Context context, ActivityMonitor activityMonitor) {
        this.context = context;
        this.activityMonitor = activityMonitor;
        uninstallerActivities = new HashSet<>();
        packageManager = context.getPackageManager();
        notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);

        Uri thisAppPackageUri = SystemIntents.packageUri(context);
        uninstallerIntents = Arrays.asList(
            new Intent(Intent.ACTION_UNINSTALL_PACKAGE, thisAppPackageUri),
            new Intent(Intent.ACTION_DELETE, thisAppPackageUri)
        );
    }

    @Override
    protected void start() {
        IntentFilter installedAppsChangedFilter = new IntentFilter();
        installedAppsChangedFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installedAppsChangedFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        installedAppsChangedFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        installedAppsChangedFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        installedAppsChangedFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installedAppsChangedFilter.addDataScheme("package");
        context.registerReceiver(installedAppsChanged, installedAppsChangedFilter);

        uninstallerActivities.clear();
        for (Intent uninstallerIntent : uninstallerIntents)
            addUninstallerActivitiesFor(uninstallerIntent);

        activityMonitor.subscribe(activityChanged);
    }

    @Override
    protected void stop() {
        activityMonitor.unsubscribe(activityChanged);
        context.unregisterReceiver(installedAppsChanged);
    }

    private void addUninstallerActivitiesFor(Intent intent) {
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {
            ComponentName componentName = ComponentNameExtensions.makeFrom(resolveInfo.activityInfo);
            uninstallerActivities.add(componentName);
        }
    }

    // Older versions of Android let apps ship with their own uninstall activities,
    // so we'll need to monitor changes to the installed apps to make sure we're
    // always up-to-date on which uninstallers to detect.
    private final BroadcastReceiver installedAppsChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isStarted())
                return;

            // Determine which app package changed, if known
            String changedAppPackage;
            Uri intentData = intent.getData();
            if (intentData != null)
                changedAppPackage = intentData.getSchemeSpecificPart();
            else
                changedAppPackage = null;

            // Forget any uninstallers from that package
            Iterator<ComponentName> uninstallerActivitiesIterator = uninstallerActivities.iterator();
            while (uninstallerActivitiesIterator.hasNext()) {
                ComponentName uninstallerActivity = uninstallerActivitiesIterator.next();
                if (changedAppPackage == null || uninstallerActivity.getPackageName().equals(changedAppPackage))
                    uninstallerActivitiesIterator.remove();
            }

            // Re-detect any uninstallers from that package
            for (Intent uninstallerIntent : uninstallerIntents) {
                Intent uninstallerIntentForApp = new Intent(uninstallerIntent).setPackage(changedAppPackage);
                addUninstallerActivitiesFor(uninstallerIntentForApp);
            }
        }
    };

    private final EventHandler activityChanged = new EventHandler() {
        @Override
        public void update() {
            ComponentName currentActivity = activityMonitor.currentOrNull();
            if (uninstallerActivities.contains(currentActivity)) {
                String title = context.getString(R.string.tip_uninstalling_title, context.getText(R.string.app_name_full));

                NotificationBuilderCompat builder = NotificationBuilderFactory.create(context)
                        .setSmallIcon(R.drawable.notification_error)
                        .setContentIntent(UninstallTipActivity.makePendingIntent(context, PendingIntentIds.UNINSTALL_TIP))
                        .setContentTitle(title)
                        .setContentText(context.getString(R.string.tip_uninstalling_description, context.getText(R.string.app_name_full)))
                        .setTicker(title) //Helps ensure the user actually notices this. Only works on Android 4.
                        .setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
                        .setChannelId(NotificationChannels.ID_TIPS);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < 26)
                    // Android 5 doesn't seem to use tickers by default. Heads-up notifications seem to be
                    // the closest equivalent. Providing a sound or vibration seems necessary to trigger them.
                    builder.setVibrate(new long[]{});

                Notification notification = builder.build();

                notificationManager.notify(NotificationIds.UNINSTALL, notification);
            }
        }
    };

}
