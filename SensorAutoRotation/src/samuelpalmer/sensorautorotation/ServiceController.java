package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.components.RotationService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ServiceController {
	private final Context context;
	
	public ServiceController(Context context) {
		this.context = context;
	}
	
	public void ensureStarted() {
		ComponentName service = context.startService(intent());
        if (service == null)
        	throw new RuntimeException("Service doesn't exist");
	}

	public void ensureStopped() {
		context.stopService(intent());
	}

	private Intent intent() {
		return new Intent(context, RotationService.class);
	}
}
