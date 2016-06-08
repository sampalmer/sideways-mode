package samuelpalmer.sensorautorotation.processes.service;

import android.util.Log;

import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;

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

	public static final UpdatingEventRegistrar onServiceStateChange = new UpdatingEventRegistrar();

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
			throw new RuntimeException("Already running");

		setInstance(instance);

		Log.i(TAG, "Service started");
	}

	public static void stopped() {
		if (!isRunning())
			throw new RuntimeException("Not running");

		setInstance(null);

		Log.i(TAG, "Service stopped");
	}

	private static void setInstance(RotationService instance) {
		ServiceHolder.instance = instance;
		onServiceStateChange.report();
	}
}
