package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;

/**
 * Basically responsible for keeping the background logic running and preventing
 * the process from being killed. Typically tied 1-1 to an {@link ApplicationInstance},
 * but in rare circumstances there may be multiple instances of the service.
 */
public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ServiceRegistrar.instance.serviceStarted(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null) {
			ApplicationInstanceCommand command = ApplicationInstanceCommand.fromIntent(intent);
			
			if (ApplicationInstanceManager.isApplicationRunning())
				ApplicationInstanceManager.applicationInstance().handle(command);
			
			// Not really sure what to do if the application isn't running...
		}

		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		ServiceRegistrar.instance.serviceStopped(this);
	}
	
}
