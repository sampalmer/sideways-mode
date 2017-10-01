package samuelpalmer.sensorautorotation.processes.service.userinteractivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.StateMonitor;

import static android.content.Context.POWER_SERVICE;

public class UserInteractivityMonitor extends StateMonitor<UserInteractivity> {

	public UserInteractivityMonitor(Listener<UserInteractivity> listener) {
		super(listener);
	}

	@Override
	protected void startInternal() {
		CurrentProcess.context().registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		CurrentProcess.context().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}

	@Override
	protected void stop() {
		CurrentProcess.context().unregisterReceiver(screenOffReceiver);
		CurrentProcess.context().unregisterReceiver(screenOnReceiver);
	}

	@Override
	protected UserInteractivity get() {
		return new UserInteractivity(isInteractive());
	}

	private boolean isInteractive() {

		if (Build.VERSION.SDK_INT >= 20)
			return ((PowerManager) CurrentProcess.context().getSystemService(POWER_SERVICE)).isInteractive();
		else
			//noinspection deprecation
			return ((PowerManager) CurrentProcess.context().getSystemService(POWER_SERVICE)).isScreenOn();
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
