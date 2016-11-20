package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;

public class ServiceRevivalManager extends State {
	private final ServiceReviver reviver;

	public ServiceRevivalManager() {
		reviver = new ServiceReviver();
	}

	@Override
	protected void start() {
		CurrentProcess.context().registerComponentCallbacks(callback);
	}

	@Override
	protected void stop() {
		CurrentProcess.context().unregisterComponentCallbacks(callback);
		reviver.ensureStopped();
	}

	private final ComponentCallbacks callback = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}

		@Override
		public void onLowMemory() {
			reviver.ensureStarted();
			Log.i(ServiceReviver.class.getSimpleName(), "Detected low system RAM. Prepared monitoring to revive the service.");
		}
	};
}
