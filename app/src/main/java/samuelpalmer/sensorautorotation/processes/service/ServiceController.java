package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import samuelpalmer.sensorautorotation.CurrentProcess;

public abstract class ServiceController {

	public static void updateServiceState(boolean enabled) {
		if (enabled)
			ensureStarted(intent());
		else
			ensureStopped();
	}

	private static void ensureStarted(Intent intent) {
		ComponentName name;
		if (Build.VERSION.SDK_INT >= 26)
			name = CurrentProcess.context().startForegroundService(intent); // So the OS doesn't block the service start due to it begin a "background" app
		else
			name = CurrentProcess.context().startService(intent);
		
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}

	private static void ensureStopped() {
		// It's important that we try to stop the service even if it doesn't look like it's running
		// since maybe the service has started but hasn't yet been stored in ServiceHolder.
		CurrentProcess.context().stopService(intent());
	}

	private static Intent intent() {
		return new Intent(CurrentProcess.context(), RotationService.class);
	}

}
