package samuelpalmer.sensorautorotation.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

import static android.content.Context.POWER_SERVICE;

public class DeviceInteractivityMonitor extends EventRegistrarAdaptor {
	
	private final Context context;
	
	private boolean isUpToDate = false;
	private boolean last;
	
	public DeviceInteractivityMonitor(Context context) {
		this.context = context;
	}
	
	@Override
	protected void subscribeToAdaptee() {
		context.registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		context.registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	@Override
	protected void unsubscribeFromAdaptee() {
		context.unregisterReceiver(screenOffReceiver);
		context.unregisterReceiver(screenOnReceiver);
		isUpToDate = false; // We're no longer subscribed, so the last received value might be out-of-date.
	}
	
	public boolean isInteractive() {
		if (isUpToDate)
			return last;
		else
			return poll();
	}
	
	private boolean poll() {
		PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
		
		if (Build.VERSION.SDK_INT >= 20)
			return powerManager.isInteractive();
		else
			//noinspection deprecation
			return powerManager.isScreenOn();
	}

	private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (hasSubscriptions()) {
				last = true;
				isUpToDate = true;
				report();
			}
		}
	};

	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (hasSubscriptions()) {
				last = false;
				isUpToDate = true;
				report();
			}
		}
	};

}
