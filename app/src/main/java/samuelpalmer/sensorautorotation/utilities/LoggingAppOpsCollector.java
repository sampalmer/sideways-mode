package samuelpalmer.sensorautorotation.utilities;

import android.app.AppOpsManager;
import android.app.AsyncNotedAppOp;
import android.app.SyncNotedAppOp;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Writes to the system log for each noted app op. This is useful to audit usage of
 * potentially-dangerous permissions by third-party code.
 *
 * As this writes a lot of log noise, make sure to only turn it on when needed.
 */
@RequiresApi(30)
public class LoggingAppOpsCollector extends AppOpsManager.OnOpNotedCallback {
    private static final String TAG = LoggingAppOpsCollector.class.getSimpleName();

    @Override
    public void onNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
        // Logging an exception so that the call stack is output to help narrow down the problematic code
        Log.i(TAG, String.format("App op %s noted for this package inside of a two-way binder call", syncNotedAppOp.getOp()), new Exception());
    }

    @Override
    public void onSelfNoted(@NonNull SyncNotedAppOp syncNotedAppOp) {
        // Logging an exception so that the call stack is output to help narrow down the problematic code
        Log.i(TAG, String.format("This app noted app op %s for its own package", syncNotedAppOp.getOp()), new Exception());
    }

    @Override
    public void onAsyncNoted(@NonNull AsyncNotedAppOp asyncNotedAppOp) {
        Log.i(TAG, String.format("Async app op %s noted for this package by uid %d with message %s", asyncNotedAppOp.getOp(), asyncNotedAppOp.getNotingUid(), asyncNotedAppOp.getMessage()));
    }
}
