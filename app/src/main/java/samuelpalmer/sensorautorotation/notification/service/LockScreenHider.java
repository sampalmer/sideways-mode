package samuelpalmer.sensorautorotation.notification.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import samuelpalmer.sensorautorotation.State;

/**
 * Updates the service notification to try to prevent it from showing on the lock screen on Android 5
 */
public class LockScreenHider extends State {

	private final Context context;
	private final ServiceNotification serviceNotification;

	public LockScreenHider(Context context, ServiceNotification serviceNotification) {
		this.context = context;
		this.serviceNotification = serviceNotification;
	}

	@Override
	protected void start() {
		if (android5())
			registerReceivers();
	}

	@Override
	protected void stop() {
		if (android5())
			context.unregisterReceiver(notificationUpdateReceiver);
	}

	private boolean android5() {
		return Build.VERSION.SDK_INT >= 21;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private void registerReceivers() {
		String[] actions = new String[]{
				Intent.ACTION_SCREEN_OFF,
				Intent.ACTION_SCREEN_ON,
				Intent.ACTION_USER_PRESENT,
				Intent.ACTION_USER_BACKGROUND,
				Intent.ACTION_USER_FOREGROUND
		};

		for (String action : actions)
			context.registerReceiver(notificationUpdateReceiver, new IntentFilter(action));
	}

	private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			serviceNotification.set();
		}
	};

}
