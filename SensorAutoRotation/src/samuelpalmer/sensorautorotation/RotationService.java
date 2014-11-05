package samuelpalmer.sensorautorotation;


import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class RotationService extends Service {

	private boolean paused;
	private Screen screen;
	private ProximitySensor tracker;
	private OrientationSensorHandler orientationListener;
	private BroadcastReceiver screenStateChangeReceiver;
	private OrientationEnforcingMonitor monitor;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		registerErrorHandler();
		
		screen = new Screen(new SystemSettings(new AndroidSystemSettingSource(this.getContentResolver())));
		OrientationCalculator calculator = new OrientationCalculator(screen);
		tracker = new ProximitySensor(this, calculator);
		orientationListener = new OrientationSensorHandler(this, calculator);
		monitor = new OrientationEnforcingMonitor(this);
			
		resume();
		supportScreenStateChanges();
		promoteToForeground();
	}

	private void registerErrorHandler() {
		final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
				//TODO: Get this working. The app still goes unstable after this stuff.
				
				if (screen != null)
					try {
						screen.close();
					} catch (Exception e) {
					}
				
				Throwable cause = paramThrowable.getCause();
				if (EnvironmentException.class.isInstance(cause))
					show(cause.getMessage());
				else
					if (oldHandler != null) {
						oldHandler.uncaughtException(paramThread, paramThrowable);
						return;
					}
				
				System.exit(0);
			}
			
			private void show(String message) {
				Notification notification = new NotificationCompat.Builder(RotationService.this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(makePending(makeIntent(ErrorDialogActivity.class).putExtra(ErrorDialogActivity.MESSAGE_INTENT_EXTRA_NAME, message)))
					.setContentTitle(getString(R.string.app_name))
					.setContentText(message)
					.setAutoCancel(true)
					.build();
				
				NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		        notificationManager.notify(0, notification);
			}
		});
	}
	
	private void resume() {
		tracker.enable();
		orientationListener.enable();
		monitor.start();

		paused = false;
	}
	
	private void pause() {
		monitor.stop();
		orientationListener.disable();
		tracker.disable();
		
		paused = true;
	}
	
	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(makePending(MainActivity.class))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Running")
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.addAction(0, "Stop", makePending(StopServiceActivity.class))
				.build();
		startForeground(1, notification);
	}

	private PendingIntent makePending(Class<?> clazz) {
		Intent intent = makeIntent(clazz);
		return makePending(intent);
	}

	private PendingIntent makePending(Intent intent) {
		return PendingIntent.getActivity(this, 0, intent, 0);
	}

	private Intent makeIntent(Class<?> clazz) {
		return new Intent(this, clazz);
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
