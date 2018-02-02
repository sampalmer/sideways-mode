package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationCommandReceiver;
import samuelpalmer.sensorautorotation.utilities.State;


public class AppDisabler extends State {
	
	private final Context context;
	
	public AppDisabler(Context context) {
		this.context = context;
	}
	
	@Override
	protected void start() {
		context.registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
	}
	
	@Override
	protected void stop() {
		context.unregisterReceiver(shutdownReceiver);
	}
	
	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (isStarted())
				ApplicationCommandReceiver.turnOffApp(context, "Device is shutting down");
		}
		
	};
	
}
