package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class RotationService extends Service {

	private boolean paused;
	private Screen screen;
	private PhysicalOrientationTracker tracker;
	private BroadcastReceiver screenStateChangeReceiver;
	private RotatorOrientationEventListener orientationListener;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			screen = new Screen(this.getContentResolver());
			final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
					screen.close();
					if (oldHandler != null)
						oldHandler.uncaughtException(paramThread, paramThrowable);
					else
						System.exit(2);
				}
			});
			AutoRotator rotator = new AutoRotator(screen);
			tracker = new PhysicalOrientationTracker(this, rotator);
			orientationListener = new RotatorOrientationEventListener(this, rotator);
			
			resume();
			supportScreenStateChanges();
			promoteToForeground();
		} catch (EnvironmentException e) {
			exitError(e.getMessage());
			return;
		}

	}
	
	private void resume() throws EnvironmentException {
		tracker.enable();
		orientationListener.enable();
		
		paused = false;
	}
	
	private void pause() {
		tracker.disable();
		orientationListener.disable();
		
		paused = true;
	}
	
	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(
						PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Running")
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.addAction(0, "Stop", PendingIntent.getActivity(this, 0, new Intent(this, StopServiceActivity.class), 0))
				.build();
		startForeground(1, notification);
	}

	private void supportScreenStateChanges() {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		screenStateChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (action.equals(Intent.ACTION_SCREEN_OFF))
					pause();
				else if (action.equals(Intent.ACTION_SCREEN_ON)) {
					try {
						resume();
					} catch (EnvironmentException e) {
						exitError(e.getMessage());
					}
				}
			}
		};

		registerReceiver(screenStateChangeReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		destroy();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		destroy();
	}

	private void destroy() {
		unregisterReceiver(screenStateChangeReceiver);

		if (!paused)
			pause();

		screen.close();
	}

	private void exitError(String message) {
		String fullMessage = getString(R.string.app_name) + ": " + message;
		Toast.makeText(this, fullMessage, Toast.LENGTH_LONG).show();
		stopSelf();
	}
}
