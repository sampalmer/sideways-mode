package samuelpalmer.sensorautorotation.processes.service.screen;

import android.util.Log;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ScreenRotationMonitor extends State {

	private ChangeDetector changeDetector;
	private final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();

	public EventRegistrar onChange() {
		return onChange;
	}

	public ScreenRotationMonitor() {

		try {
			changeDetector = new RotationWatcherChangeDetector(callback);
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
		}
	}

	@Override
	protected void start() {
		try {
			changeDetector.start();
		} catch (RotationWatcherChangeDetector.NotSupportedException e) {
			fallback(e);
			start();
		}
	}

	@Override
	protected void stop() {
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
			onChange.report();
		}
	};

}
