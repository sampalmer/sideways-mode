package samuelpalmer.sensorautorotation.processes.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationCommandReceiver;


public class AppDisabler extends State {
	
	private final KeyguardManager keyguardManager;
	private final Context context;
	
	public AppDisabler() {
		context = CurrentProcess.context();
		keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
	}
	
	@Override
	protected void start() {
		context.registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
		context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
		context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		if (Build.VERSION.SDK_INT >= 17) {
			context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_BACKGROUND));
			context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_FOREGROUND));
		}
		
		context.registerReceiver(unlockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));
	}
	
	@Override
	protected void stop() {
		context.unregisterReceiver(unlockReceiver);
		context.unregisterReceiver(screenChangedReceiver);
		context.unregisterReceiver(shutdownReceiver);
	}
	
	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			ApplicationCommandReceiver.turnOffApp("Device is shutting down");
		}
		
	};
	
	private final BroadcastReceiver screenChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isOnLockScreen = LockScreenMonitor.poll(keyguardManager);
			if (isOnLockScreen)
				ApplicationCommandReceiver.turnOffApp("Device is locked");
		}
	};
	
	private final BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// We receive this broadcast after the device gets unlocked.
			// This implies that the device was locked, so the app shouldn't be on.
			// We need this check because Android doesn't broadcast when the device gets locked,
			// and our polling approach might sometimes be too slow to detect the lock screen.
			// For example, fingerprint unlock on my Motorola Moto G5 Plus unlocks the device
			// so quickly that you don't see the lock screen.
			ApplicationCommandReceiver.turnOffApp("Device was locked");
		}
	};
	
}
