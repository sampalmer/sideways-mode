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
			
			// Intentionally not doing anything to recover here because:
			// 1. The app should operate for as long as possible so screen orientation doesn't get stuck
			// 2. Using hacks to keep the process alive could have side effects such as being the cause of Android having multiple simultaneous instances of the service.
		}
	};
	
}
