package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class LockScreenMonitor extends State {

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();

	public EventRegistrar<Void> possiblyChanged() {
		return onChange;
	}

	@Override
	protected void start() {
		for (String action : actions())
			ThisApplication.context().registerReceiver(notificationUpdateReceiver, new IntentFilter(action));
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

	@Override
	protected void stop() {
		ThisApplication.context().unregisterReceiver(notificationUpdateReceiver);
	}

	private final BroadcastReceiver notificationUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onChange.report(null);
		}
	};
}
