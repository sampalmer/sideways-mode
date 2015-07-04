package samuelpalmer.sensorautorotation.settings.application.remote.processprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.ArrayList;

import samuelpalmer.sensorautorotation.BroadcastKillFix;
import samuelpalmer.sensorautorotation.Process;

class MultiProcessChangeReceiver extends BroadcastReceiver {

	public static final String EXTRA_SHARED_PREFS_NAME = "sharedprefs name";
	public static final String EXTRA_PREF_KEYS = "pref keys";

	public static void broadcastChange(String preferencesName, ArrayList<String> keys) {
		Intent intent = new Intent(action())
			.putExtra(EXTRA_SHARED_PREFS_NAME, preferencesName)
			.putStringArrayListExtra(EXTRA_PREF_KEYS, keys);

		BroadcastKillFix.apply(intent);

		Process.context().sendBroadcast(intent);
	}

	private final MultiProcessSharedPreferences prefs;
	private final OnSharedPreferenceChangeListener listener;

	public MultiProcessChangeReceiver(MultiProcessSharedPreferences prefs, OnSharedPreferenceChangeListener listener) {
		this.prefs = prefs;
		this.listener = listener;
	}

	public void register() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(action());
		Process.context().registerReceiver(this, filter);
	}

	public void unregister() {
		Process.context().unregisterReceiver(this);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String preferencesName = intent.getStringExtra(EXTRA_SHARED_PREFS_NAME);

		if (preferencesName.equals(prefs.preferencesName)) {
			ArrayList<String> keys = intent.getStringArrayListExtra(EXTRA_PREF_KEYS);

			for (String key : keys)
				listener.onSharedPreferenceChanged(prefs, key);
		}
	}

	private static String action() {
		return MultiProcessChangeReceiver.class.getName();
	}

}
