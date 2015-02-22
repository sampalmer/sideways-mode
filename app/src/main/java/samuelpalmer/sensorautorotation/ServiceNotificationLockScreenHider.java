package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.Arrays;
import java.util.List;

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
		List<String> actions = Arrays.asList(
			Intent.ACTION_SCREEN_OFF,
			Intent.ACTION_SCREEN_ON,
			Intent.ACTION_USER_PRESENT
		);

		if (Build.VERSION.SDK_INT >= 17)
			actions.addAll(Arrays.asList(
				Intent.ACTION_USER_BACKGROUND,
				Intent.ACTION_USER_FOREGROUND
			));

		for (String action : actions)
			context.registerReceiver(notificationUpdateReceiver, new IntentFilter(action));
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
