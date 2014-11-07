package samuelpalmer.sensorautorotation;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class RotationService extends Service {

	private boolean paused;
	private Screen screen;
	private ProximitySensor proximityListener;
	private OrientationSensorHandler orientationListener;
	private BroadcastReceiver screenStateChangeReceiver;
	private Intents intents;
	private UserErrorHandler errorHandler;
	private boolean destroyed;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		intents = new Intents(this);
		
		try {
			screen = new Screen(new SystemSettings(new AndroidSystemSettingSource(this.getContentResolver())));
			OrientationCalculator calculator = new OrientationCalculator(screen);
			proximityListener = new ProximitySensor(this, calculator);
			orientationListener = new OrientationSensorHandler(this, calculator);
				
			resume();
			supportScreenStateChanges();
			promoteToForeground();
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
	}

	private void resume() {
		try {
			proximityListener.enable();
		} catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
		
		orientationListener.enable();

		paused = false;
	}
	
	private void pause() {
		if (orientationListener != null)
			orientationListener.disable();
		if (proximityListener != null)
			proximityListener.disable();
		
		paused = true;
	}
	
	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(intents.makePending(MainActivity.class))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Running")
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.addAction(0, "Stop", intents.makePending(StopServiceActivity.class))
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
		if (!destroyed) {
			if (screenStateChangeReceiver != null)
				unregisterReceiver(screenStateChangeReceiver);
			
			if (!paused)
				pause();
			
			if (screen != null)
				try {
					screen.close();
				} catch (EnvironmentException e) {
					errorHandler.handle(e);
				}
			
			destroyed = true;
		}
	}
}
