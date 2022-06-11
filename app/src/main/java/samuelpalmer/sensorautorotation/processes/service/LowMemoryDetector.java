package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Handles rare situations in which the OS is running out of memory
 */
public class LowMemoryDetector extends State {
    
    private final Context context;
    
    public LowMemoryDetector(Context context) {
        this.context = context;
    }
    
    @Override
    protected void start() {
        context.registerComponentCallbacks(callback);
    }

    @Override
    protected void stop() {
        context.unregisterComponentCallbacks(callback);
    }

    private final ComponentCallbacks callback = new ComponentCallbacks() {
        @Override
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override
        public void onLowMemory() {
            if (isStarted()) {
                /*
                This is rare, but it can happen if an app is extremely memory intensive or if the OS has memory leaks.
                This situation is a warning that the OS might kill the process.
                If the OS kills the process, the service notification will disappear and screen rotation won't work.
                The OS may schedule a restart of the service a couple of minutes after killing it.
                */
                
                Log.w(LowMemoryDetector.class.getSimpleName(), "Detected low system RAM. The OS might kill this process soon.");
                
                // Not stopping the app here because the user hasn't asked to stop. If the OS kills the process, the user will just have to wait for the OS to restart the service or restart it manually.
                // Not using hacks to keep the process alive because it could have side effects such as causing Android to having multiple simultaneous instances of the service.
            }
        }
    };
    
}
