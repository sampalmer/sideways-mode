package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;

/**
 * Basically responsible for keeping the background logic running and preventing
 * the process from being killed. Typically tied 1-1 to an {@link ApplicationInstance},
 * but in rare circumstances there may be multiple instances of the service,
 * presumably due to OS bugs.
 */
public class RotationService extends ServiceThatWorksAroundTaskRemovalBug {
	
	public static final String EXTRA_INITIAL_USER_SIDE = "initial_user_side";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		UserSide initialUserSide = null;
		if (intent != null && intent.hasExtra(EXTRA_INITIAL_USER_SIDE)) // Service was started explicitly by user
			initialUserSide = UserSide.valueOf(intent.getStringExtra(EXTRA_INITIAL_USER_SIDE));
		
		if (intent != null && intent.getAction() != null) { // Service is started from a remote commmand
			ServiceRegistrar.instance.serviceStarted(this, initialUserSide);
			
			ApplicationInstanceCommand command = ApplicationInstanceCommand.fromIntent(intent);
			
			if (ApplicationInstanceManager.isApplicationRunning())
				ApplicationInstanceManager.applicationInstance().handle(command);
			
			// Not really sure what to do if the application isn't running...
		}
		else // Service is restarting after being killed or similar
			ServiceRegistrar.instance.serviceStarted(this, initialUserSide);
		
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
