package samuelpalmer.sensorautorotation.processes.service.screen;

import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrarAdaptor;

public class ScreenRotationMonitor extends EventRegistrarAdaptor {

	private ChangeDetector changeDetector;

	public ScreenRotationMonitor() {
		try {
			changeDetector = new RotationWatcherChangeDetector(callback);
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
		}
	}

	@Override
	protected void subscribeToAdaptee() {
		try {
			changeDetector.start();
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
			subscribeToAdaptee();
		}
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		try {
			changeDetector.stop();
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
		}
	}

	public interface Callback {
		void changed();
	}

	public interface ChangeDetector {
		void start();
		void stop();
	}

	private void fallback(RotationWatcherChangeDetector.NotSupportedException reason) {
		Log.w(ScreenRotationMonitor.class.getSimpleName(), reason);
		changeDetector = new ComponentCallbacksChangeDetector(callback);
	}

	private final Callback callback = new Callback() {
		@Override
		public void changed() {
			report();
		}
	};

}
