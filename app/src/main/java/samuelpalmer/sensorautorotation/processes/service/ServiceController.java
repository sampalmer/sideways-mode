package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public abstract class ServiceController {
	
	public static void ensureStarted(Context context) {
		Intent intent = new Intent(context, RotationService.class);
		
		ComponentName name;
		if (Build.VERSION.SDK_INT >= 26)
			name = context.startForegroundService(intent); // So the OS doesn't block the service start due to it being a "background" app
		else
			name = context.startService(intent);
		
		if (name == null)
			throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
	}
	
	public static void ensureStopped(Context context) {
		Intent intent = new Intent(context, RotationService.class);
		
		// It's important that we try to stop the service even if it doesn't look like it's running
		// since maybe the OS considers the service started, but it hasn't yet actually started it.
		context.stopService(intent);
	}
	
}
