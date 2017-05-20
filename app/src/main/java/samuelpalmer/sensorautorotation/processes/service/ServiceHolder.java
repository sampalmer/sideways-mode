package samuelpalmer.sensorautorotation.processes.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.util.Log;

import java.util.List;

import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.SystemServices;

/**
 * I'm using this instead of binding since this is mostly a single-threaded process and this
 * avoids the complexities of binding and inability to determine service running state.
 *
 * It should be reliable because the service maintains this value. If the service is killed by
 * the OS due to low memory or something like that, I believe the whole process is killed, which
 * should cause this value to be reset anyway.
 */
abstract class ServiceHolder {

	private static final String TAG = ServiceHolder.class.getSimpleName();
	private static RotationService instance;
	public static final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();

	public static boolean isRunning() {
		return instance != null;
	}

	public static RotationService instance() {
		if (!isRunning())
			throw new RuntimeException("Service not running");

		return instance;
	}

	public static void started(RotationService instance) {
		if (isRunning())
			throw makeServiceAlreadyRunningError(instance);
		
		ServiceHolder.instance = instance;
		onChange.report();

		Log.i(TAG, "Service started");
	}

	public static void stopped() {
		if (!isRunning())
			throw new RuntimeException("Not running");

		instance = null;
		onChange.report();

		Log.i(TAG, "Service stopped");
	}
	
	/**
	 * One user has actually run into this situation several times. This method captures
	 * some extra info to help the next time it happens.
	 */
	private static RuntimeException makeServiceAlreadyRunningError(RotationService instance) {
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
		
		if (ServiceHolder.instance == instance)
			return new RuntimeException("This instance of the service is already running.\n" + runningServiceDescription);
		else
			return new RuntimeException("Another instance of this service is running.\n" + runningServiceDescription);
	}
}
