package samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;

abstract class PollingActivityMonitor extends ActivityMonitor {

    private static final int POLL_INTERVAL_MILLIS = 500;

    private final Handler handler;

    PollingActivityMonitor(Context context) {
        super(context);
        this.handler = new Handler();
    }

    @Override
    protected void subscribeToAdaptee() {
        super.subscribeToAdaptee();
        // We have a subscription at this point, so we're good to poll straight away
        pollAndSchedule.run();
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        super.unsubscribeFromAdaptee();
        handler.removeCallbacks(pollAndSchedule);
    }

    private final Runnable pollAndSchedule = new Runnable() {
        @Override
        public void run() {
            if (hasSubscriptions()) {
                ComponentName topActivity = poll();
                setTopActivity(topActivity);
                handler.postDelayed(pollAndSchedule, POLL_INTERVAL_MILLIS);
            }
        }
    };

    abstract ComponentName poll();

}
