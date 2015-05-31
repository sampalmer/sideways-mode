package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class ServiceController {

	private static void ensureStarted() {
		if (!ServiceHolder.isRunning()) {
			ComponentName name = ThisApplication.context().startService(intent());
			if (name == null)
				throw new RuntimeException("Service doesn't exist");
		}
	}

	private static void ensureStopped() {
		if (ServiceHolder.isRunning())
			ThisApplication.context().stopService(intent());
	}

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

	private static Intent intent() {
		return new Intent(ThisApplication.context(), RotationService.class);
	}
}
