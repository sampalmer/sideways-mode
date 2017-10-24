package samuelpalmer.sensorautorotation.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

import static android.content.Context.POWER_SERVICE;

public class DeviceInteractivityMonitor extends EventRegistrarAdaptor {
	
	private boolean isUpToDate = false;
	private boolean last;
	
	@Override
	protected void subscribeToAdaptee() {
		CurrentProcess.context().registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		CurrentProcess.context().registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	@Override
	protected void unsubscribeFromAdaptee() {
		CurrentProcess.context().unregisterReceiver(screenOffReceiver);
		CurrentProcess.context().unregisterReceiver(screenOnReceiver);
		isUpToDate = false; // We're no longer subscribed, so the last received value might be out-of-date.
	}
	
	public boolean isInteractive() {
		if (isUpToDate)
			return last;
		else
			return poll();
	}
	
	private boolean poll() {
		PowerManager powerManager = (PowerManager) CurrentProcess.context().getSystemService(POWER_SERVICE);
		
		if (Build.VERSION.SDK_INT >= 20)
			return powerManager.isInteractive();
		else
			//noinspection deprecation
			return powerManager.isScreenOn();
	}

	private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			last = true;
			isUpToDate = true;
			report();
		}
	};

	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			last = false;
			isUpToDate = true;
			report();
		}
	};

}
