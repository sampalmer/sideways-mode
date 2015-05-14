package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {

	private static final String TAG = ActivityMonitor.class.getSimpleName();

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private final Context context;
	private String lastApp;

	public ActivityMonitor(Context context) {
		this.context = context;
	}

	public String current() {
		return lastApp;
	}

	public void setCurrentApp(String currentApp) {
		if (!currentApp.equals(lastApp)) {
			lastApp = currentApp;
			onChange.report(null);
			Log.i(TAG, "Current app detected: " + currentApp);
//			Toast.makeText(context, lastApp, Toast.LENGTH_SHORT).show();
		}
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		lastApp = "";
		context.registerReceiver(systemKeyPressReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(systemKeyPressReceiver);
	}

	private final BroadcastReceiver systemKeyPressReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String reason = intent.getStringExtra("reason");
			if (reason != null) {
				switch (reason) {
					case "homekey":
						trySetCurrentApp(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
						break;
					case "recentapps": //Recents activity
					case "globalactions": //Power menu (long press power button)
					case "assist": //Don't know what triggers this
						break;
				}
			}
		}
	};

	private void trySetCurrentApp(Intent intent) {
		ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (resolveInfo != null) {
			String packageName = resolveInfo.activityInfo.packageName;
			setCurrentApp(packageName);
		}
	}
}
