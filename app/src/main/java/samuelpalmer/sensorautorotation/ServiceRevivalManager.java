package samuelpalmer.sensorautorotation;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;

public class ServiceRevivalManager extends State {
	private final ServiceReviver reviver;

	public ServiceRevivalManager() {
		reviver = new ServiceReviver();
	}

	@Override
	protected void start() {
		ThisApplication.context().registerComponentCallbacks(callback);
	}

	@Override
	protected void stop() {
		ThisApplication.context().unregisterComponentCallbacks(callback);
		reviver.ensureStopped();
	}

	private final ComponentCallbacks callback = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}

		@Override
		public void onLowMemory() {
			reviver.ensureStarted();
		}
	};
}
