package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.processes.service.screen.RotationWatcherRegistrar.RotationWatcherNotSupportedException;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.values.Orientation;

import static android.content.Context.WINDOW_SERVICE;

public class ScreenRotationMonitor extends EventRegistrarAdaptor {
	
	private final Context context;
	private final Display screen;
	private ChangeDetector changeDetector;

	public ScreenRotationMonitor(Context context) {
		this.screen = ((WindowManager) context.getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		this.context = context;
		
		try {
			changeDetector = new RotationWatcherChangeDetector(callback, screen);
		} catch (RotationWatcherNotSupportedException e) {
			fallback(e);
		}
	}

	public Orientation currentRotation() {
		try {
			return changeDetector.currentRotation();
		} catch (RotationWatcherNotSupportedException e) {
			if (!noSubscriptions()) {
				try {
					changeDetector.stop();
				} catch (RotationWatcherNotSupportedException ignored) {
					//All good; we're already handling this
				}
			}

			fallback(e);

			if (!noSubscriptions())
				subscribeToAdaptee();

			return currentRotation();
		}
	}

	@Override
	protected void subscribeToAdaptee() {
		try {
			changeDetector.start();
		} catch (RotationWatcherNotSupportedException e) {
			fallback(e);
			subscribeToAdaptee();
		}
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		try {
			changeDetector.stop();
		} catch (RotationWatcherNotSupportedException e) {
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

	private void fallback(RotationWatcherNotSupportedException reason) {
		Log.w(ScreenRotationMonitor.class.getSimpleName(), reason);
		changeDetector = new ComponentCallbacksChangeDetector(context, callback, screen);
	}

	private final Callback callback = new Callback() {
		@Override
		public void changed() {
			report();
		}
	};

}
