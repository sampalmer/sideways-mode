package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;

public abstract class ServiceController {

	public static void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted(intent());
		else
			ensureStopped();
	}

	public static void ensureStarted(Intent intent) {
		ComponentName name = CurrentProcess.context().startService(intent);
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}

	private static void ensureStopped() {
		// It's important that we try to stop the service even if it doesn't look like it's running
		// since maybe the service has started but hasn't yet been stored in ServiceHolder.
		CurrentProcess.context().stopService(intent());
	}

	public static Intent intent() {
		return ComponentType.create(RotationService.class).intent();
	}

}
