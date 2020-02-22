package samuelpalmer.sensorautorotation.utilities;

import android.app.AppOpsManager;
import android.app.AsyncNotedAppOp;
import android.app.SyncNotedAppOp;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Writes warnings to the system log for each noted app op
 */
@RequiresApi(29)
public class LoggingAppOpsCollector extends AppOpsManager.AppOpsCollector {
    private static final String TAG = LoggingAppOpsCollector.class.getSimpleName();

    @Override
    public void onNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
        // Logging an exception so that the call stack is output to help narrow down the problematic code
        Log.w(TAG, new Exception(String.format("App op %s noted for this package inside of a two-way binder call", syncNotedAppOp.getOp())));
    }

    @Override
    public void onSelfNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
        // Logging an exception so that the call stack is output to help narrow down the problematic code
        Log.w(TAG, new Exception(String.format("This app noted app op %s for its own package", syncNotedAppOp.getOp())));
    }

    @Override
    public void onAsyncNoted(@NonNull AsyncNotedAppOp asyncNotedAppOp) {
        Log.w(TAG, String.format("Async app op %s noted for this package by uid %d with message %s", asyncNotedAppOp.getOp(), asyncNotedAppOp.getNotingUid(), asyncNotedAppOp.getMessage()));
    }
}
