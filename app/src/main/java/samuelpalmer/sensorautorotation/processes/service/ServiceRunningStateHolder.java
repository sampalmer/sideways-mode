package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

/**
 * Keeps track of the {@link ServiceRunningState}
 */
public abstract class ServiceRunningStateHolder {
    
    public static final UpdatingEventRegistrar onStartedOrStopped = new UpdatingEventRegistrar();
    
    /**
     * I'm using a global instead of Android's binding mechanism.
     * This is because the service's process is mostly a single-threaded process and it
     * avoids the complexities of binding and inability to determine service running state.
     */
    private static ServiceRunningState instance;
    
    public static void serviceStarted(ServiceRunningState service) {
        if (instance != null)
            throw new RuntimeException("Already have an instance");
        
        instance = service;
        
        onStartedOrStopped.report();
    }
    
    public static void serviceStopped() {
        if (instance == null)
            throw new RuntimeException("Already stopped");
        
        instance = null;
        
        onStartedOrStopped.report();
    }
    
    public static boolean isServiceRunning() {
        return instance != null;
    }
    
    public static ServiceRunningState getServiceInstance() {
        if (instance == null)
            throw new IllegalStateException("The service is not running.");
        
        return instance;
    }
    
}
