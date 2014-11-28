package samuelpalmer.sensorautorotation.components;


import samuelpalmer.sensorautorotation.App;
import samuelpalmer.sensorautorotation.EnvironmentException;
import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ScreenOrientationController;
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
	private ScreenOrientationController screenOrientationController;
	private ScreenOnMonitor screenOnMonitor;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		screenOrientationController = new ScreenOrientationController(this);
		app = new App(this, screenOrientationController);
		screenOnMonitor = new ScreenOnMonitor(this, new StateMonitor.Listener<Boolean>() {
			@Override
			public void stateChanged(Boolean on) {
				try {
					if (on)
						app.ensureStarted();
					else
						app.ensureStopped();
				} catch (EnvironmentException e) {
					new UserErrorHandler(RotationService.this).handle(e);
				}
			}
		});
		
		registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		try {
			screenOrientationController.ensureStarted();
		
			promoteToForeground();
			
			screenOnMonitor.ensureStarted();
		} catch (EnvironmentException e) {
			new UserErrorHandler(this).handle(e);
		}
	}

	private void destroy() {
		if (!destroyed) {
			try {
				app.ensureStopped();
				screenOrientationController.ensureStopped();
				unregisterReceiver(shutdownReceiver);
				screenOnMonitor.ensureStopped();
			} catch (EnvironmentException e) {
				new UserErrorHandler(this).handle(e);
			}
			
			destroyed = true;
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
