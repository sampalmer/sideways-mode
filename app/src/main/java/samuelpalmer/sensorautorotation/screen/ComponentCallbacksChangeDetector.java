package samuelpalmer.sensorautorotation.screen;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;

import samuelpalmer.sensorautorotation.ThisApplication;

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
		ThisApplication.context().registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		ThisApplication.context().unregisterComponentCallbacks(componentCallbacks);
	}

	private ComponentCallbacks componentCallbacks = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			callback.changed();
		}

		@Override
		public void onLowMemory() {
		}
	};
}
