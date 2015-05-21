package samuelpalmer.sensorautorotation.screen;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Only detects changes between landscape and portrait.
 */
class ComponentCallbacksChangeDetector implements ScreenRotationMonitor.ChangeDetector {

	private final Context context;
	private final ScreenRotationMonitor.Callback callback;

	public ComponentCallbacksChangeDetector(Context context, ScreenRotationMonitor.Callback callback) {
		this.context = context;
		this.callback = callback;
	}

	@Override
	public void start() {
		context.registerComponentCallbacks(componentCallbacks);
	}

	@Override
	public void stop() {
		context.unregisterComponentCallbacks(componentCallbacks);
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
