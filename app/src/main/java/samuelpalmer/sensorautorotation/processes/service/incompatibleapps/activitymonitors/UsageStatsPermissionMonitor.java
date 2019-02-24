package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import androidx.annotation.RequiresApi;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class UsageStatsPermissionMonitor extends EventRegistrarAdaptor {

    private final AppOpsManager appOps;
    private final int uid;
    private final String packageName;
    private final Handler handler;

    private boolean isUpToDate = false;
    private boolean hadPermission;

    UsageStatsPermissionMonitor(Context context) {
        // This permission check is taken from https://medium.com/@quiro91/show-app-usage-with-usagestatsmanager-d47294537dab
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        uid = Process.myUid();
        packageName = context.getPackageName();
        handler = new Handler();
    }

    boolean hasPermission() {
        if (isUpToDate)
            return hadPermission;
        else
            return poll();
    }

    @Override
    protected void subscribeToAdaptee() {
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS, packageName, usageOpListener);
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        appOps.stopWatchingMode(usageOpListener);
        handler.removeCallbacks(checkUsagePermission);
        isUpToDate = false;
    }

    private final AppOpsManager.OnOpChangedListener usageOpListener = new AppOpsManager.OnOpChangedListener() {
        @Override
        public void onOpChanged(String op, String packageName) {
            // Not in main thread, so post to main thread queue
            handler.post(checkUsagePermission);
        }
    };

    private final Runnable checkUsagePermission = new Runnable() {
        @Override
        public void run() {
            if (hasSubscriptions()) {
                boolean hasPermission = poll();
                if (!isUpToDate || hasPermission != hadPermission) {
                    hadPermission = hasPermission;
                    isUpToDate = true;
                    report();
                }
            }
        }
    };

    private boolean poll() {
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, packageName);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
