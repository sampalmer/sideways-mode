package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ServiceController {
	private final Context context;
	
	public ServiceController(Context context) {
		this.context = context;
	}
	
	public void start() {
		ComponentName service = context.startService(intent());
        if (service == null)
        	throw new RuntimeException("Service doesn't exist");
	}

	public void stop() {
		context.stopService(intent());
	}

	private Intent intent() {
		return new Intent(context, RotationService.class);
	}
}
