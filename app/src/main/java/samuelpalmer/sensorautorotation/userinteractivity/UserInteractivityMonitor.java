package samuelpalmer.sensorautorotation.userinteractivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.ThisApplication;

public class UserInteractivityMonitor extends StateMonitor<UserInteractivity> {

	public UserInteractivityMonitor(Listener<UserInteractivity> listener) {
		super(listener);
	}

	@Override
	protected void startInternal() {
		ThisApplication.context().registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		ThisApplication.context().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	protected void stop() {
		ThisApplication.context().unregisterReceiver(screenOffReceiver);
		ThisApplication.context().unregisterReceiver(screenOnReceiver);
	}

	@Override
	protected UserInteractivity get() {
		return new UserInteractivity(isInteractive());
	}

	private boolean isInteractive() {

		if (Build.VERSION.SDK_INT >= 20)
			return SystemServices.powerManager().isInteractive();
		else
			//noinspection deprecation
			return SystemServices.powerManager().isScreenOn();
	}

	private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			reportChange(new UserInteractivity(true));
		}
	};

	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			reportChange(new UserInteractivity(false));
		}
	};

}
