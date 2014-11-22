package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenOnMonitor extends StateMonitor<Boolean> {

	private final Context context;

	public ScreenOnMonitor(Context context, Listener<Boolean> listener) {
		super(listener);
		this.context = context;
	}
	
	@Override
	protected void startInternal() {
		context.registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		context.registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
	
	@Override
	protected void stop() {
		context.unregisterReceiver(screenOffReceiver);
		context.unregisterReceiver(screenOnReceiver);
	}
	
	@Override
	protected Boolean get() {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	private final BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			reportChange(true);
		}
	};
	
	private final BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			reportChange(false);
		}
	};
	
}
