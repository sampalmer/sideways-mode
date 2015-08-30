package samuelpalmer.sensorautorotation.processes.service.screen;

import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.processes.service.Orientation;

public class ScreenRotationMonitor extends EventRegistrarAdaptor {

	private final Screen screen;
	private ChangeDetector changeDetector;

	public ScreenRotationMonitor(Screen screen) {
		this.screen = screen;

		try {
			changeDetector = new RotationWatcherChangeDetector(callback, screen);
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
		}
	}

	public Orientation currentRotation() {
		return changeDetector.currentRotation();
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
		Orientation currentRotation();
	}

	private void fallback(RotationWatcherChangeDetector.NotSupportedException reason) {
		Log.w(ScreenRotationMonitor.class.getSimpleName(), reason);
		changeDetector = new ComponentCallbacksChangeDetector(callback, screen);
	}

	private final Callback callback = new Callback() {
		@Override
		public void changed() {
			report();
		}
	};

}
