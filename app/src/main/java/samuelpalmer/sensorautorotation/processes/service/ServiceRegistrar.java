package samuelpalmer.sensorautorotation.processes.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.SystemServices;

/**
 * Keeps track of all running instances of {@link RotationService}.
 * This shouldn't be needed, but in rare circumstances Android has multiple
 * instances of the service running.
 *
 * It should be reliable because the service maintains this value. If the service is killed by
 * the OS due to low memory or something like that, I believe the whole process is killed, which
 * should cause this value to be reset anyway.
 */
public class ServiceRegistrar extends UpdatingEventRegistrar {
	
	public static final ServiceRegistrar instance = new ServiceRegistrar();
	
	private static final String TAG = ServiceRegistrar.class.getSimpleName();
	private final Set<RotationService> serviceInstances;
	private final Set<RotationService> readonlyServiceInstances;
	
	private ServiceRegistrar() {
		serviceInstances = new HashSet<>();
		readonlyServiceInstances = Collections.unmodifiableSet(serviceInstances);
	}
	
	public Set<RotationService> getServiceInstances() {
		return readonlyServiceInstances;
	}
	
	public void serviceStarted(RotationService service) {
		if (!serviceInstances.isEmpty()) {
			// This should never happen, but it actually did happen occasionally on Production devices, presumably due to Android OS bugs.
			// So the app tries to tolerate this.
			Log.w(TAG, makeServiceAlreadyRunningError(service));
		}
		
		serviceInstances.add(service);
		report();
		
		Log.i(TAG, "Service started: " + service);
	}
	
	public void serviceStopped(RotationService service) {
		boolean wasInSet = serviceInstances.remove(service);
		if (!wasInSet)
			throw new RuntimeException("Unknown service stopped: " + service);
		
		report();
		
		Log.i(TAG, "Service stopped: " + service);
	}
	
	/**
	 * Several users have actually run into this situation multiple times. This method captures
	 * some extra info to help the next time it happens.
	 */
	private String makeServiceAlreadyRunningError(RotationService service) {
		ComponentName rotationServiceComponent = new ComponentName(CurrentProcess.context(), RotationService.class);
		
		List<ActivityManager.RunningServiceInfo> runningServices = SystemServices.activityManager().getRunningServices(Integer.MAX_VALUE);
		int serviceCount = 0;
		StringBuilder runningServiceDescription = new StringBuilder("Running instances:\n");
		
		for (ActivityManager.RunningServiceInfo runningService : runningServices) {
			if (rotationServiceComponent.equals(runningService.service)) {
				++serviceCount;
				
				runningServiceDescription.append(serviceCount).append(".activeSince = ").append(runningService.activeSince).append('\n');
				runningServiceDescription.append(serviceCount).append(".crashCount = ").append(runningService.crashCount).append('\n');
				runningServiceDescription.append(serviceCount).append(".foreground = ").append(runningService.foreground).append('\n');
				runningServiceDescription.append(serviceCount).append(".lastActivityTime = ").append(runningService.lastActivityTime).append('\n');
				runningServiceDescription.append(serviceCount).append(".restarting = ").append(runningService.restarting).append('\n');
				runningServiceDescription.append(serviceCount).append(".flags = ").append(runningService.flags).append('\n');
				runningServiceDescription.append(serviceCount).append(".started = ").append(runningService.started).append('\n');
			}
		}
		
		if (serviceInstances.contains(service))
			return "This instance of the service is already running.\n" + runningServiceDescription;
		else
			return "Another instance of this service is running.\n" + runningServiceDescription;
	}
	
}
