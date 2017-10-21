package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public abstract class ServiceHolder {
	
	public static final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	
	/**
	 * I'm using a global instead of Android's binding mechanism.
	 * This is because the service's process is mostly a single-threaded process and it
	 * avoids the complexities of binding and inability to determine service running state.
	 */
	private static RotationService instance;
	
	public static void serviceStarted(RotationService service) {
		if (instance != null)
			throw new RuntimeException("Already have an instance");
		
		instance = service;
		
		onChange.report();
	}
	
	public static void serviceStopped() {
		if (instance == null)
			throw new RuntimeException("Already stopped");
		
		instance = null;
		
		onChange.report();
	}
	
	public static boolean isServiceRunning() {
		return instance != null;
	}
	
	public static RotationService getServiceInstance() {
		if (instance == null)
			throw new IllegalStateException("The service is not running.");
		
		return instance;
	}
	
}
