package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceController {

	private void ensureStarted() {
		if (!ServiceHolder.isRunning()) {
			ComponentName name = ThisApplication.context().startService(intent());
			if (name == null)
				throw new RuntimeException("Service doesn't exist");
		}
	}

	private void ensureStopped() {
		if (ServiceHolder.isRunning())
			ThisApplication.context().stopService(intent());
	}

	public void updateFromSettings() {
		boolean enabled = new ApplicationSettings().enabled().get();
		updateServiceState(enabled);
	}

	private void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted();
		else
			ensureStopped();
	}

	private Intent intent() {
		return new Intent(ThisApplication.context(), RotationService.class);
	}
}
