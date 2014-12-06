package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class UserInteractivityMonitor extends StateMonitor<UserInteractivity> {

	private final Context context;

	public UserInteractivityMonitor(Context context, Listener<UserInteractivity> listener) {
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
	protected UserInteractivity get() {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		return new UserInteractivity(pm.isScreenOn());
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
