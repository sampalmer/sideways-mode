package samuelpalmer.sensorautorotation.processes.service;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.utilities.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;


/**
 * Stays open until the accessibility service detects what the currently-open activity is.
 * We can't just stop the activity as soon as it starts because sometimes it takes
 * a bit more time for the OS to notify the accessibility service.
 * (Stopping the activity too soon results in the accessibility service missing it altogether.)
 */
public class WaitForActivityDetectionActivity extends Activity {
	
	private static final String TAG = WaitForActivityDetectionActivity.class.getSimpleName();
	private static final int TIMEOUT_SECONDS = 30;
	
	private Handler handler;
	
	/**
	 * Whether we're subscribed to "activity change" notifications.
	 * We need to track this so we know whether to unsubscribe when we finish,
	 * since the service could stop while we're still subscribed.
	 */
	private boolean isSubscribed;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		isSubscribed = false;
		handler = new Handler();
		
		ServiceRunningStateHolder.onStartedOrStopped.subscribe(serviceStartedOrStopped);
		
		if (!new AccessibilityServiceEnabledStateMonitor(this, WindowChangeDetectingService.class).isEnabled()) {
			Log.w(TAG, "Accessibility service is turned off. Finishing activity.");
			finish();
		}
		else if (!ServiceRunningStateHolder.isServiceRunning()) {
			Log.w(TAG, "Service is not running. Finishing activity.");
			finish();
		}
		else if (WindowChangeDetectingService.getCurrentActivity() != null) {
			Log.i(TAG, "Detected activity change. Finishing activity.");
			finish();
		}
		else {
			WindowChangeDetectingService.onActivityChange().subscribe(onActivityChanged);
			isSubscribed = true;
			
			handler.postDelayed(onTimeout, TIMEOUT_SECONDS * 1000);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		handler.removeCallbacks(onTimeout);
		
		if (isSubscribed && ServiceRunningStateHolder.isServiceRunning()) {
			WindowChangeDetectingService.onActivityChange().unsubscribe(onActivityChanged);
			isSubscribed = false;
		}
		
		ServiceRunningStateHolder.onStartedOrStopped.unsubscribe(serviceStartedOrStopped);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//Prevents the error: "Activity {samuelpalmer.sensorautorotation/samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity} did not call finish() prior to onResume() completing"
		if (Build.VERSION.SDK_INT >= 23)
			setVisible(true);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		if (!isFinishing()) {
			Log.w(TAG, "Activity stopped. Finishing activity.");
			finish();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// This prevents the activity from animating out when it closes.
		overridePendingTransition(0, 0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isFinishing() && event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.w(TAG, "User touched screen. Finishing activity.");
			finish();
			return true;
		}
		
		return super.onTouchEvent(event);
	}
	
	private final EventHandler serviceStartedOrStopped = () -> {
		if (!isFinishing() && !ServiceRunningStateHolder.isServiceRunning()) {
			Log.w(TAG, "Service stopped running. Finishing activity.");
			isSubscribed = false;
			finish();
		}
	};
	
	private final EventHandler onActivityChanged = () -> {
		if (!isFinishing()) {
			ComponentName currentActivity = WindowChangeDetectingService.getCurrentActivity();
			if (currentActivity != null) {
				// Doesn't matter which activity was detected; main thing is that *some* change was detected, so now our job is done.
				Log.i(TAG, "Detected activity change. Finishing activity.");
				finish();
			} else {
				// The accessibility service is needed to detect activity changes, so without it, this activity isn't relevant.
				Log.w(TAG, "Accessibility service disconnected. Finishing activity.");
				finish();
			}
		}
	};
	
	private final Runnable onTimeout = () -> {
		if (!isFinishing()) {
			Log.w(TAG, "Activity was not detected for " + TIMEOUT_SECONDS + "s. Finishing activity.");
			finish();
		}
	};

}
