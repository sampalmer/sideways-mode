package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.view.Display;

import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Uses {@link ComponentCallbacks} to detect changes to screen orientation.
 * This only detects changes between landscape and portrait; it won't detect 180 degree changes
 * due to limitations of {@link ComponentCallbacks}.
 */
class ComponentCallbacksChangeDetector implements ScreenRotationMonitor.ChangeDetector {
	
	private final Context context;
	private final ScreenRotationMonitor.Callback callback;
	private final Display screen;
	
	public ComponentCallbacksChangeDetector(Context context, ScreenRotationMonitor.Callback callback, Display screen) {
		this.context = context;
		this.callback = callback;
		this.screen = screen;
	}

	@Override
	public void start() {
		context.registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		context.unregisterComponentCallbacks(componentCallbacks);
	}

	@Override
	public Orientation currentRotation() {
		return AndroidSurfaceRotation.toOrientationClockwise(screen.getRotation());
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
