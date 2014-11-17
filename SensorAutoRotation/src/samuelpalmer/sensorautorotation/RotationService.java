package samuelpalmer.sensorautorotation;


import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

public class RotationService extends Service {

	private boolean destroyed;
	private App app;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		
		promoteToForeground();
		
		if (screenOn())
			startProcessing();
	}

	private void destroy() {
		if (!destroyed) {
			stopProcessing();
			
			unregisterReceiver(shutdownReceiver);
			unregisterReceiver(screenOffReceiver);
			unregisterReceiver(screenOnReceiver);
			
			destroyed = true;
		}
	}
	
	private void startProcessing() {
		if (app == null)
			app = new App(this);
	}
	
	private void stopProcessing() {
		if (app != null) {
			app.destroy();
			app = null;
		}
	}
	
	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			stopSelf();
		}
	};
	
	private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startProcessing();
		}
	};
	
	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			stopProcessing();
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private boolean screenOn() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Intents intents = new Intents(this);
		
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(intents.makePending(MainActivity.class))
				.setContentTitle(getString(R.string.app_name))
				.setContentText("Running")
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.build();
		
		startForeground(1, notification);
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
	
}
