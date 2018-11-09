package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Tracks which screen orientations get used.
 */
public class SupportedOrientationsDetector extends EventRegistrarAdaptor {

	/**
		We can only count a screen orientation if it is held for this long. This is needed to eliminate:
		- The orientation changing as part of an activity change transition
		- The orientation flicking back and forth due to {@link samuelpalmer.sensorautorotation.processes.service.WaitForActivityDetectionActivity}
	 	- The orientation flicking back and forth when turning off screen orientation enforcement while switching to an activity that doesn't support rotation
	 */
	private static final int MIN_DURATION = 1000;
	
	private final Context context;
	private final Handler handler;
	
	private boolean supportsPortrait;
	private boolean supportsLandscape;
	private int orientation;
	
	public SupportedOrientationsDetector(Context context) {
		this.context = context;
		handler = new Handler();
	}
	
	public boolean supportsPortrait() {
		if (!hasSubscriptions())
			throw new IllegalStateException();
		
		return supportsPortrait;
	}
	
	public boolean supportsLandscape() {
		if (!hasSubscriptions())
			throw new IllegalStateException();
		
		return supportsLandscape;
	}
	
	public void reset() {
		if (!hasSubscriptions())
			throw new IllegalStateException();
		
		supportsPortrait = false;
		supportsLandscape = false;
		handler.removeCallbacks(orientationHeld);
		report();
		
		handler.postDelayed(orientationHeld, MIN_DURATION);
	}
	
	@Override
	protected void subscribeToAdaptee() {
		context.registerComponentCallbacks(componentCallbacks);
		
		supportsPortrait = false;
		supportsLandscape = false;
		orientation = context.getResources().getConfiguration().orientation;
		
		handler.postDelayed(orientationHeld, MIN_DURATION);
	}
	
	@Override
	protected void unsubscribeFromAdaptee() {
		handler.removeCallbacks(orientationHeld);
		
		context.unregisterComponentCallbacks(componentCallbacks);
	}
	
	private final ComponentCallbacks componentCallbacks = new ComponentCallbacks() {
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			if (hasSubscriptions() && orientation != newConfig.orientation) {
				orientation = newConfig.orientation;
				
				handler.removeCallbacks(orientationHeld);
				handler.postDelayed(orientationHeld, MIN_DURATION);
			}
		}
		
		@Override
		public void onLowMemory() {}
	};
	
	private final Runnable orientationHeld = new Runnable() {
		@Override
		public void run() {
			if (hasSubscriptions()) {
				if (orientation == Configuration.ORIENTATION_PORTRAIT) {
					supportsPortrait = true;
					report();
				} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
					supportsLandscape = true;
					report();
				}
			}
		}
	};
	
}
