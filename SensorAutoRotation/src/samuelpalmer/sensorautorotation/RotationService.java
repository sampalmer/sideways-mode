package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemConfig;
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
	private RotatorOrientationEventListener orientationListener;
	private BroadcastReceiver screenStateChangeReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		registerErrorHandler();
		
		screen = new Screen(new SystemSettings(new AndroidSystemConfig(this.getContentResolver())));
		OrientationCalculator calculator = new OrientationCalculator(screen);
		tracker = new PhysicalOrientationTracker(this, calculator);
		orientationListener = new RotatorOrientationEventListener(this, calculator);
		
		resume();
		supportScreenStateChanges();
		promoteToForeground();
	}

	private void registerErrorHandler() {
		final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
				if (screen != null)
				try {
					screen.close();
				} catch (Exception e) {
				}
				
				if (EnvironmentException.class.isInstance(paramThrowable))
					exitError(paramThrowable.getMessage());
				else
					if (oldHandler != null)
						oldHandler.uncaughtException(paramThread, paramThrowable);
					else
						System.exit(2);
			}
			
			private void exitError(String message) {
				String fullMessage = getString(R.string.app_name) + ": " + message;
				Toast.makeText(getApplicationContext(), fullMessage, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void resume() {
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
				else if (action.equals(Intent.ACTION_SCREEN_ON))
					resume();
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
}
