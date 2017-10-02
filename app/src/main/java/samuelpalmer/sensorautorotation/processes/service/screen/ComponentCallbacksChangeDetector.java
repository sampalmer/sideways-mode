package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.ComponentCallbacks;
import android.content.res.Configuration;
import android.view.Display;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * Only detects changes between landscape and portrait.
 */
class ComponentCallbacksChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final ScreenRotationMonitor.Callback callback;
	private final Display screen;

	public ComponentCallbacksChangeDetector(ScreenRotationMonitor.Callback callback, Display screen) {
		this.callback = callback;
		this.screen = screen;
	}

	@Override
	public void start() {
		CurrentProcess.context().registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		CurrentProcess.context().unregisterComponentCallbacks(componentCallbacks);
	}

	@Override
	public Orientation currentRotation() {
		return new AndroidSurfaceRotation(screen.getRotation()).orientation();
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
