package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.processes.service.screen.RotationWatcherRegistrar.RotationWatcherNotSupportedException;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Detects when the device's default display's screen rotation changes.
 * Note that subscribers are not guaranteed to be notified of 180 degree changes
 * due to OS limitations.
 */
public class ScreenRotationMonitor extends EventRegistrarAdaptor {
	
	private final Context context;
	private final Display screen;
	private ChangeDetector changeDetector;

	public ScreenRotationMonitor(Context context) {
		this.screen = ContextCompat.getSystemService(context, WindowManager.class).getDefaultDisplay();
		this.context = context;
		
        try {
            changeDetector = new RotationWatcherChangeDetector(this::report, screen);
        } catch (RotationWatcherNotSupportedException e) {
            fallback(e);
        }
	}

	public Orientation currentRotation() {
		try {
			return changeDetector.currentRotation();
		} catch (RotationWatcherNotSupportedException e) {
			if (hasSubscriptions()) {
				try {
					changeDetector.stop();
				} catch (RotationWatcherNotSupportedException ignored) {
					//All good; we're already handling this
				}
			}

			fallback(e);
			
			if (hasSubscriptions())
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
		changeDetector = new ComponentCallbacksChangeDetector(context, this::report, screen);
	}
	
}
