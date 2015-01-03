package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.components.RotationService;

public class ServiceController {
	private final Context context;
	private final Settings settings;

	public ServiceController(Context context) {
		this.context = context;
		settings = new Settings(context);
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
		boolean enabled = settings.enabled().getOr(true);
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
