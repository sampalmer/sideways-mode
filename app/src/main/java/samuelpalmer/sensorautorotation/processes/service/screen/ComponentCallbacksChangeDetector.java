package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.processes.service.Orientation;

/**
 * Only detects changes between landscape and portrait.
 */
class ComponentCallbacksChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final ScreenRotationMonitor.Callback callback;
	private final Screen screen;

	public ComponentCallbacksChangeDetector(ScreenRotationMonitor.Callback callback, Screen screen) {
		this.callback = callback;
		this.screen = screen;
	}

	@Override
	public void start() {
		Process.context().registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		Process.context().unregisterComponentCallbacks(componentCallbacks);
	}

	@Override
	public Orientation currentRotation() {
		return screen.realOrientation();
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
