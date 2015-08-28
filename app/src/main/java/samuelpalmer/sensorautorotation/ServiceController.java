package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class ServiceController {

	public static void updateFromSettings() {
		updateServiceState(serviceEnabled());
	}

	private static void updateServiceState(boolean enabled) {
		if (enabled)
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

	private static void ensureStopped() {
		Process.context().stopService(intent());
	}

	public static Intent intent() {
		return ComponentType.create(RotationService.class).intent();
	}

	public static void enableService() {
		setEnabled(true);
	}

	public static void disableService() {
		setEnabled(false);
	}

	private static void setEnabled(boolean value) {
		ApplicationSettings.instance().enabled().setAndCommit(value);
		updateServiceState(value);
	}
}
