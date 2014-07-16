package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class RotationService extends Service {
	
	private boolean paused;
	private OrientationChanger orientationChanger;
	private PhysicalOrientationTracker tracker;
	private BroadcastReceiver screenStateChangeReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate()
    {
		super.onCreate();
		
		try {
			resume();
	        supportScreenStateChanges();
			promoteToForeground();
		} catch (EnvironmentException e) {
			exitError(e.getMessage());
			return;
		}
		
    }
	
	private void resume() throws EnvironmentException {
		orientationChanger = new PassiveOrientationChanger(this.getContentResolver());
		OrientationCalculator orientationCalculator = new OrientationCalculator(this.orientationChanger.getOriginal());
		
		SensorManager sensors = (SensorManager)getSystemService(SENSOR_SERVICE);
		SensorDataHandler dataHandler = new SensorDataHandler(orientationCalculator, orientationChanger);
		tracker = new PhysicalOrientationTracker(sensors, dataHandler);
		
		paused = false;
	}

	/**
	 * Reduces chance of being killed
	 */
	private void promoteToForeground() {
		Notification notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
			.setContentTitle(getString(R.string.app_name))
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
	}

	private void pause() {
		tracker.close();
		orientationChanger.close();
		
		paused = true;
	}
	
	private void exitError(String message) {
		String fullMessage = getString(R.string.app_name) + ": " + message;
		Toast.makeText(this, fullMessage, Toast.LENGTH_LONG)
			.show();
		stopSelf();
	}
}
