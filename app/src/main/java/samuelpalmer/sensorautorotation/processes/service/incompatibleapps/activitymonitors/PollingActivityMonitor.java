package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

abstract class PollingActivityMonitor extends ActivityMonitor {

    private static final int POLL_INTERVAL_MILLIS = 500;

    private final Handler handler;

    PollingActivityMonitor(Context context) {
        super(context);
        this.handler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    @Override
    protected void subscribeToAdaptee() {
        super.subscribeToAdaptee();
        pollAndSchedule(false); // Not sending an update since this is not a *change*, just the initial value
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        super.unsubscribeFromAdaptee();
        handler.removeCallbacks(poller);
    }

    private final Runnable poller = () -> {
        if (hasSubscriptions())
            pollAndSchedule(true);
    };

    private void pollAndSchedule(boolean sendUpdate) {
        ComponentName topActivity = poll();
        setTopActivity(topActivity, sendUpdate);
        handler.postDelayed(poller, POLL_INTERVAL_MILLIS);
    }

    abstract ComponentName poll();

}
