package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class ServiceController {

	public static void updateFromSettings() {
		updateServiceState();
	}

	private static void updateServiceState() {
		if (serviceEnabled())
			ensureStarted(intent());
		else
			ensureStopped();
	}

	public static boolean serviceEnabled() {
		return ApplicationSettings.instance().enabled().get();
	}

	public static void ensureStarted(Intent intent) {
		ComponentName name = Process.context().startService(intent);
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}

	public static void ensureStopped() {
		Process.context().stopService(intent());
	}

	public static Intent intent() {
		return ComponentType.create(RotationService.class).intent();
	}

	public static void disableService() {
		ApplicationSettings.instance().enabled().setAndCommit(false);
		ensureStopped();
	}
}
