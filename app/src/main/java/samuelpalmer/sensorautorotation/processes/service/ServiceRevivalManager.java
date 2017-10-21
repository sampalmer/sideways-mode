package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;

public class ServiceRevivalManager extends State {

	@Override
	protected void start() {
		CurrentProcess.context().registerComponentCallbacks(callback);
	}

	@Override
	protected void stop() {
		CurrentProcess.context().unregisterComponentCallbacks(callback);
	}

	private final ComponentCallbacks callback = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}

		@Override
		public void onLowMemory() {
			Log.i(ServiceRevivalManager.class.getSimpleName(), "Detected low system RAM. The OS might kill this process soon.");
			
			// Not stopping the app here because the user hasn't asked to stop. If the OS kills the process, the user will just have to wait for the OS to restart the service.
			// Not using hacks to keep the process alive because it could have side effects such as causing Android to having multiple simultaneous instances of the service.
		}
	};
	
}
