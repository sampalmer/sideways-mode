package samuelpalmer.sensorautorotation.utilities;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import androidx.annotation.RequiresApi;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Monitors changes to a particular app op.
 * @see AppOpsManager
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class AppOpMonitor extends EventRegistrarAdaptor {

    private final AppOpsManager appOps;
    private final int uid;
    private final String packageName;
    private final Handler handler;
    private final String op;

    private boolean isUpToDate = false;
    private boolean hadPermission;

    /**
     * @param op The <code>OPSTR_</code> constant from {@link AppOpsManager} for the op you want to monitor
     */
    public AppOpMonitor(Context context, String op) {
        // Not using ContextCompat.getSystemService() because it doesn't work on older versions of Android (as far as I recall)
        appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        uid = Process.myUid();
        packageName = context.getPackageName();
        handler = new Handler();
        this.op = op;
    }

    public boolean hasPermission() {
        if (isUpToDate)
            return hadPermission;
        else
            return poll();
    }

    @Override
    protected void subscribeToAdaptee() {
        appOps.startWatchingMode(op, packageName, usageOpListener);
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
            // We get callbacks for op changes across *all* apps
            if (AppOpMonitor.this.packageName.equals(packageName)) {
                // Not in main thread, so post to main thread queue
                handler.post(checkUsagePermission);
            }
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
        int mode = appOps.checkOpNoThrow(op, uid, packageName);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
