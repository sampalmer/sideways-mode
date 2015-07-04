package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class ServiceController {

	public static void updateFromSettings() {
		boolean enabled = new ApplicationSettings().enabled().get();
		updateServiceState(enabled);
	}

	private static void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted();
		else
			ensureStopped();
	}

	private static void ensureStarted() {
		ComponentName name = Process.context().startService(intent());
		if (name == null)
			throw new RuntimeException("Service doesn't exist");
	}

	private static void ensureStopped() {
		Process.context().stopService(intent());
	}

	private static Intent intent() {
		return ComponentType.create(RotationService.class).intent();
	}
}
