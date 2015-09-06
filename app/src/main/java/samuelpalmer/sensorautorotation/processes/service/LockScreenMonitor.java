package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrarAdaptor;

public class LockScreenMonitor extends EventRegistrarAdaptor {

	private Boolean isOnLockScreen;
	private boolean monitoring;

	public boolean isOnLockScreen() {
		if (!monitoring)
			return poll();
		else {
			if (isOnLockScreen == null)
				isOnLockScreen = poll();
			return isOnLockScreen;
		}
	}

	private static boolean poll() {
		if (Build.VERSION.SDK_INT >= 16)
			return SystemServices.keyguardManager().isKeyguardLocked();
		else
			return SystemServices.keyguardManager().inKeyguardRestrictedInputMode();
	}

	@Override
	protected void subscribeToAdaptee() {
		for (String action : actions())
			Process.context().registerReceiver(notificationUpdateReceiver, new IntentFilter(action));
		monitoring = true;
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		monitoring = false;
		isOnLockScreen = null;
		Process.context().unregisterReceiver(notificationUpdateReceiver);
	}

	private Iterable<String> actions() {
		List<String> result = new ArrayList<>(Arrays.asList(
				Intent.ACTION_SCREEN_OFF,
				Intent.ACTION_SCREEN_ON,
				Intent.ACTION_USER_PRESENT
		));

		if (Build.VERSION.SDK_INT >= 17)
			result.addAll(Arrays.asList(
					Intent.ACTION_USER_BACKGROUND,
					Intent.ACTION_USER_FOREGROUND
			));

		return result;
	}

	private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean newValue = poll();
			if (isOnLockScreen == null || isOnLockScreen != newValue) {
				isOnLockScreen = newValue;
				report();
			}
		}
	};
}
