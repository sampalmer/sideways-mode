package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;

import samuelpalmer.sensorautorotation.Process;

/**
 * Only detects changes between landscape and portrait.
 */
class ComponentCallbacksChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final ScreenRotationMonitor.Callback callback;

	public ComponentCallbacksChangeDetector(ScreenRotationMonitor.Callback callback) {
		this.callback = callback;
	}

	@Override
	public void start() {
		Process.context().registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		Process.context().unregisterComponentCallbacks(componentCallbacks);
	}

	private final ComponentCallbacks componentCallbacks = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			callback.changed();
		}

		@Override
		public void onLowMemory() {
		}
	};
}
