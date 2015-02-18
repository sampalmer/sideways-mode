package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Updates the service notification to try to prevent it from showing on the lock screen on Android 5
 */
public class ServiceNotificationLockScreenHider extends State {

	private final Context context;
	private final ServiceNotification notification;

	public ServiceNotificationLockScreenHider(Context context, ServiceNotification notification) {

		this.context = context;
		this.notification = notification;
	}

	@Override
	protected void start() {
		context.registerReceiver(notificationUpdateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		context.registerReceiver(notificationUpdateReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		context.registerReceiver(notificationUpdateReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(notificationUpdateReceiver);
	}

	private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			notification.set();
		}
	};

}
