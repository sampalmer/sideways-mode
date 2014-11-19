package samuelpalmer.sensorautorotation.components;


import samuelpalmer.sensorautorotation.App;
import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.Screen;
import samuelpalmer.sensorautorotation.ScreenOnMonitor;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.UserErrorHandler;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class RotationService extends Service {

	private boolean destroyed;
	private App app;
	private Screen screen;
	private ScreenOnMonitor screenOnMonitor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		screenOnMonitor = new ScreenOnMonitor(this, new StateMonitor.Listener<Boolean>() {
			@Override
			public void stateChanged(Boolean on) {
				if (on)
					startProcessing();
				else
					stopProcessing();
			}
		});
		
		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		try {
			screen = new Screen(this);
		} catch (EnvironmentException e) {
			new UserErrorHandler(this).handle(e);
		}
		
		promoteToForeground();
		
		screenOnMonitor.start();
	}

	private void destroy() {
		if (!destroyed) {
			stopProcessing();
			
			try {
				screen.close();
			} catch (EnvironmentException e) {
				new UserErrorHandler(this).handle(e);
			}
			unregisterReceiver(shutdownReceiver);
			screenOnMonitor.stop();
			
			destroyed = true;
		}
	}
	
	private void startProcessing() {
		if (app == null)
			app = new App(this, screen);
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
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
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
