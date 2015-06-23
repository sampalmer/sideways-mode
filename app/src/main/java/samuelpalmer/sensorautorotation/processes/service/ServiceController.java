package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

abstract class ServiceController {

	private static void ensureStarted() {
		ComponentName name = samuelpalmer.sensorautorotation.Process.context().startService(intent());
		if (name == null)
			throw new RuntimeException("Service doesn't exist");
	}

	private static void ensureStopped() {
		Process.context().stopService(intent());
	}

	public static void updateFromSettings() {
		boolean enabled = new ApplicationSettings(Prefs.prefs()).enabled().get();
		updateServiceState(enabled);
	}

	private static void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted();
		else
			ensureStopped();
	}

	private static Intent intent() {
		return new Intents(RotationService.class).makeIntent();
	}
}
