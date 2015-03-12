package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.settings.ApplicationSettings;

public class ServiceController {
	private final Context context;
	private final ApplicationSettings settings;

	public ServiceController(Context context) {
		this.context = context;
		settings = new ApplicationSettings(context);
	}

	public void ensureStarted() {
		ComponentName service = context.startService(intent());
		if (service == null)
			throw new RuntimeException("Service doesn't exist");
	}

	public void ensureStopped() {
		context.stopService(intent());
	}

	public void updateFromSettings() {
		boolean enabled = settings.enabled().get();
		updateServiceState(enabled);
	}

	private void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted();
		else
			ensureStopped();
	}

	private Intent intent() {
		return new Intent(context, RotationService.class);
	}
}
