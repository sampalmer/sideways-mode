package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class ServiceController {

	public static void updateFromSettings() {
		boolean enabled = ApplicationSettings.instance().enabled().get();
		updateServiceState(enabled);
	}

	private static void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted(intent());
		else
			ensureStopped();
	}

	public static void ensureStarted(Intent intent) {
		ComponentName name = Process.context().startService(intent);
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}

	private static void ensureStopped() {
		Process.context().stopService(intent());
	}

	public static Intent intent() {
		return ComponentType.create(RotationService.class).intent();
	}
}
