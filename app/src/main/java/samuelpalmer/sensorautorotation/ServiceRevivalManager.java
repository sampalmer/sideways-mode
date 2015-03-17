package samuelpalmer.sensorautorotation;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;

public class ServiceRevivalManager extends State {
	private final Context context;
	private ServiceReviver reviver;

	public ServiceRevivalManager(Context context) {
		this.context = context;
		reviver = new ServiceReviver(context);
	}

	@Override
	protected void start() {
		context.registerComponentCallbacks(callback);
	}

	@Override
	protected void stop() {
		context.unregisterComponentCallbacks(callback);
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
