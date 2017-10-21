package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;

import samuelpalmer.sensorautorotation.CurrentProcess;

public abstract class ServiceController {
	
	public static void ensureStarted() {
		Intent intent = new Intent(CurrentProcess.context(), RotationService.class);
		
		ComponentName name;
		if (Build.VERSION.SDK_INT >= 26)
			name = CurrentProcess.context().startForegroundService(intent); // So the OS doesn't block the service start due to it being a "background" app
		else
			name = CurrentProcess.context().startService(intent);
		
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}
	
	public static void ensureStopped() {
		Intent intent = new Intent(CurrentProcess.context(), RotationService.class);
		
		// It's important that we try to stop the service even if it doesn't look like it's running
		// since maybe the OS considers the service started, but it hasn't yet actually started it.
		CurrentProcess.context().stopService(intent);
	}
	
}
