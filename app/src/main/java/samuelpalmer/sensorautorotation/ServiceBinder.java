package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import samuelpalmer.sensorautorotation.components.RotationService;

public class ServiceBinder {
	private final ServiceConnection connection;

	public ServiceBinder(ServiceConnection connection) {
		this.connection = connection;
	}

	public void bind() {
		Intent intent = new Intent(ThisApplication.context(), RotationService.class);
		boolean bound = ThisApplication.context().bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
		if (!bound)
			//This seems to happen when the service being bound to isn't installed.
			throw new RuntimeException("Couldn't bind to service");
	}

	public void unbind() {
		ThisApplication.context().unbindService(connection);
	}
}
