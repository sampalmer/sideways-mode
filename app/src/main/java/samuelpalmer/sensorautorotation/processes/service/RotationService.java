package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import samuelpalmer.sensorautorotation.ApplicationCommand;

/**
 * Basically responsible for keeping the background logic running and preventing
 * the process from being killed. Typically tied 1-1 to an {@link ApplicationInstance},
 * but in rare circumstances there may be multiple instances of the service,
 * presumably due to OS bugs.
 */
public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {
	
	private boolean registered = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (!registered) {
			ServiceRegistrar.instance.serviceStarted(this);
			registered = true;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null && intent.getAction() != null) { // Service is started from a remote commmand
			ApplicationCommand command = new ApplicationCommand(intent);
			
			if (ApplicationInstanceManager.isApplicationRunning())
				ApplicationInstanceManager.handle(command);
			else
				Log.w(RotationService.class.getSimpleName(), "Received command, but the application isn't running");
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
		
		if (registered) {
			ServiceRegistrar.instance.serviceStopped(this);
			registered = false;
		}
	}
	
}
