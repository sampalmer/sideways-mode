package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {

    /**
     * Boolean extra supplied to intent passed to {@link #onStartCommand(Intent, int, int)} to
     * indicate that the service should use started state {@link #START_NOT_STICKY}. If not
     * specified, started state {@link #START_STICKY} will be used.
     */
    public static final String EXTRA_NOT_STICKY = "notsticky";

    private static final String TAG = RotationService.class.getSimpleName();
    
    // Used to ensure the OS doesn't create multiple simultaneous instances of the service.
    private static RotationService startedInstance;
    
    private ServiceRunningState runningState;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (startedInstance != null) {
            if (startedInstance != this) {
                // This is weird, but it actually happens occasionally in Production :(
                throw new RuntimeException("Service onStartCommand() called on a different instance of the service.");
            }
            else {
                // This may indicate a bug, or maybe it's just a race condition, which is normal.
                Log.w(TAG, "Service onStartCommand() was called a second time on the same instance.");
            }
        }
        else
            startedInstance = this;
        
        if (runningState == null) {
            runningState = new ServiceRunningState(this);
            runningState.ensureStarted();
        }

        boolean notSticky = intent != null && intent.getExtras() != null && intent.getExtras().getBoolean(EXTRA_NOT_STICKY, false);
        return notSticky ? Service.START_NOT_STICKY : Service.START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (startedInstance == null) {
            // Maybe the OS created the service but didn't get around to starting it yet.
            // Or maybe the service was being bound to but wasn't in a started state, which is fine.
            Log.w(TAG, "Service onDestroy() was called, but the service wasn't started.");
        }
        else if (startedInstance != this) {
            // This is weird, but this kind of thing actually happens occasionally in Production :(
            throw new RuntimeException("Service onDestroy() called on a different instance of the service.");
        }
        else
            startedInstance = null;
        
        if (runningState != null) {
            runningState.ensureStopped();
            runningState = null;
        }
    }
    
}
