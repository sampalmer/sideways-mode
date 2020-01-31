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
        appOps.startWatchingMode(op, packageName, opListener);
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        appOps.stopWatchingMode(opListener);
        handler.removeCallbacks(checkPermission);
        isUpToDate = false;
    }

    private final AppOpsManager.OnOpChangedListener opListener = new AppOpsManager.OnOpChangedListener() {
        @Override
        public void onOpChanged(String op, String packageName) {
            /*
             * We op change notifications for *all* apps, not just this app.
             * Normally we would ignore ones that aren't for this ap.
             * But there's a weird bug in the Android 8.0 emulator when granting the
             * "display over other apps" permission that causes the OS to incorrectly
             * report the op as _not_ granted when it in fact is. The OS then
             * sends out a notification with a null packageName after the permission
             * activity is closed, and the OS reports the correct op status at this point.
             * So we need to check the op even if the notification isn't for this app.
             */

            if (packageName == null || AppOpMonitor.this.packageName.equals(packageName)) {
                // Android actually notifies us of changes to ops other than the one we registered for, so filtering them out
                if (AppOpMonitor.this.op.equals(op)) {
                    // We're not in main thread, so post to main thread queue
                    handler.post(checkPermission);
                }
            }
        }
    };

    private final Runnable checkPermission = new Runnable() {
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
        int mode;

        if (Build.VERSION.SDK_INT >= 29)
            mode = appOps.unsafeCheckOpNoThrow(op, uid, packageName);
        else {
            //noinspection deprecation
            mode = appOps.checkOpNoThrow(op, uid, packageName);
        }

        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
