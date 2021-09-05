package samuelpalmer.sensorautorotation.utilities;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.Context;
import android.os.Build;
import android.system.OsConstants;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.utilities.testing.FieldConstants;

public final class Diagnostics {

    private static final String TAG = Diagnostics.class.getSimpleName();

    private final Context context;

    public Diagnostics(Context context){
        this.context = context;
    }

    /**
     * Writes a message to the log explaining the reason the process last exited, if this information is available
     */
    public void logLastProcessExitReason() {
        if (Build.VERSION.SDK_INT < 30)
            return;

        ApplicationExitInfo exitReason = getLastExitReason();
        if (exitReason == null)
            return;

        String logMessage = formatExitReason(exitReason);
        Log.i(TAG, logMessage);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Nullable
    private ApplicationExitInfo getLastExitReason() {
        ActivityManager activityManager = ContextCompat.getSystemService(context, ActivityManager.class);
        assert activityManager != null;
        List<ApplicationExitInfo> exitReasons = activityManager.getHistoricalProcessExitReasons(context.getPackageName(),0,0);

        for (ApplicationExitInfo exitReason : exitReasons)
            if (CurrentProcess.getProcessName().equals(exitReason.getProcessName()))
                return exitReason;

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private static String formatExitReason(ApplicationExitInfo exitReason) {
        long timeDifferenceMillis = System.currentTimeMillis() - exitReason.getTimestamp();
        String importanceName = new FieldConstants<>(ActivityManager.RunningAppProcessInfo.class, "IMPORTANCE").getName(exitReason.getImportance());

        String reasonName = new FieldConstants<>(ApplicationExitInfo.class, "REASON").getName(exitReason.getReason());
        long rss = exitReason.getRss();

        String logMessage = String.format(
                Locale.US,
                "Process %s last exited %dms ago with importance %s and resident set size %s due to %s (%s) and status %d",
                exitReason.getProcessName(),
                timeDifferenceMillis,
                importanceName,
                rss == 0 ? "N/A" : rss + "kB",
                reasonName,
                exitReason.getDescription(),
                exitReason.getStatus()
        );

        // Apparently some OS versions cannot report REASON_LOW_MEMORY, so we're left guessing in those cases
        if (!ActivityManager.isLowMemoryKillReportSupported() && exitReason.getReason() == ApplicationExitInfo.REASON_SIGNALED && exitReason.getStatus() == OsConstants.SIGKILL)
            logMessage += ". This may be due to low memory.";

        return logMessage;
    }

}
