package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.components.RotationService;

/**
 * I'm using this instead of binding since this is mostly a single-threaded application and this
 * avoids the complexities of binding and inability to determine service running state.
 *
 * It should be reliable because the service maintains this value. If the service is killed by
 * the OS due to low memory or something like that, I believe the whole process is killed, which
 * should cause this value to be reset anyway.
 */
public abstract class ServiceHolder {

	private static RotationService instance;

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

		ServiceHolder.instance = instance;
	}

	public static void stopped() {
		if (!isRunning())
			throw new RuntimeException("Not running");

		instance = null;
	}
}
