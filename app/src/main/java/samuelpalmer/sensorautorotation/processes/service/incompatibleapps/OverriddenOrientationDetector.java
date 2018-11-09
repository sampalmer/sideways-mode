package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.view.Surface;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Detects when the current activity's screen orientation differs from {@link android.provider.Settings.System#USER_ROTATION}.
 */
public class OverriddenOrientationDetector extends EventRegistrarAdaptor {
	
	private final ContentResolver contentResolver;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final LockScreenMonitor lockScreenMonitor;
	private final Uri userRotationUri;
	private final Handler handler;
	
	private boolean overridesOrientation;
	private boolean isOverridden;
	
	public OverriddenOrientationDetector(Context context, ScreenRotationMonitor screenRotationMonitor, LockScreenMonitor lockScreenMonitor) {
		contentResolver = context.getContentResolver();
		this.screenRotationMonitor = screenRotationMonitor;
		this.lockScreenMonitor = lockScreenMonitor;
		userRotationUri = Settings.System.getUriFor(Settings.System.USER_ROTATION);
		handler = new Handler();
	}
	
	public boolean overridesOrientation() {
		if (!hasSubscriptions())
			throw new IllegalStateException();
		
		return overridesOrientation;
	}
	
	public void reset() {
		if (!hasSubscriptions())
			throw new IllegalStateException();
		
		overridesOrientation = false;
		isOverridden = false;
		handler.removeCallbacks(overrideHeld);
		report();
		
		check();
	}
	
	@Override
	protected void subscribeToAdaptee() {
		contentResolver.registerContentObserver(userRotationUri, false, requestedRotationChanged);
		screenRotationMonitor.subscribe(anythingChanged);
		lockScreenMonitor.subscribe(anythingChanged);
		
		overridesOrientation = false;
		isOverridden = false;
		
		check();
	}
	
	@Override
	protected void unsubscribeFromAdaptee() {
		handler.removeCallbacks(overrideHeld);
		
		lockScreenMonitor.unsubscribe(anythingChanged);
		screenRotationMonitor.unsubscribe(anythingChanged);
		contentResolver.unregisterContentObserver(requestedRotationChanged);
	}
	
	private final ContentObserver requestedRotationChanged = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			if (hasSubscriptions())
				check();
		}
	};
	
	private final EventHandler anythingChanged = () -> {
		if (hasSubscriptions())
			check();
	};
	
	private void check() {
		Orientation requestedRotation = AndroidSurfaceRotation.toOrientationClockwise(Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0));
		Orientation currentRotation = screenRotationMonitor.currentRotation();
		boolean isOverriddenNew =
			!requestedRotation.equals(currentRotation)
			&&
			!lockScreenMonitor.isOnLockScreen() // The lock screen might be overriding the activity's screen orientation, so we'll just ignore things while the lock screen is open.
		;
		
		if (isOverridden != isOverriddenNew) {
			isOverridden = isOverriddenNew;
			
			handler.removeCallbacks(overrideHeld);
			if (isOverridden)
				/* TODO: Somehow prevent this from firing if the screen orientation mismatch is caused by the device running slowly
					Maybe make this whole thing smarter about how to determine whether the orientation is overridden:
					- Maybe measure how long it takes for a handler callback to be called back?
				 */
				handler.postDelayed(overrideHeld, 1000);
		}
	}
	
	private final Runnable overrideHeld = () -> {
		if (hasSubscriptions()) {
			overridesOrientation = true;
			report();
		}
	};
	
}
