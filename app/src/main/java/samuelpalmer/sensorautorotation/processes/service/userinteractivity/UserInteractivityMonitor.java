package samuelpalmer.sensorautorotation.processes.service.userinteractivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrarAdaptor;

import static android.content.Context.POWER_SERVICE;

public class UserInteractivityMonitor extends EventRegistrarAdaptor {
	
	private UserInteractivity last;
	
	@Override
	protected void subscribeToAdaptee() {
		CurrentProcess.context().registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		CurrentProcess.context().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	@Override
	protected void unsubscribeFromAdaptee() {
		CurrentProcess.context().unregisterReceiver(screenOffReceiver);
		CurrentProcess.context().unregisterReceiver(screenOnReceiver);
	}
	
	public UserInteractivity get() {
		if (last == null)
			return new UserInteractivity(isInteractive());
		else
			return last;
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
			last = new UserInteractivity(true);
			report();
		}
	};

	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			last = new UserInteractivity(false);
			report();
		}
	};

}
