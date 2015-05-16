package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.orientationenforcement.currentapplicationretrieval.CurrentApplicationRetriever;

public class ActivityMonitor extends State {

	private static final String TAG = ActivityMonitor.class.getSimpleName();

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private final Context context;
	private final CurrentApplicationRetriever currentApplicationRetriever;
	private final Set<String> lastApp = new HashSet<>(3); //I don't expect to pick up a process with more than 3 apps in it

	public ActivityMonitor(Context context) {
		this.context = context;
		currentApplicationRetriever = CurrentApplicationRetriever.create(context);
	}

	public Set<String> current() {
		return lastApp;
	}

	public void setCurrentApp(String... currentApps) {
		Set<String> currentPackages = new HashSet<>(Arrays.asList(currentApps));
		if (!currentPackages.equals(lastApp)) {
			lastApp.clear();
			lastApp.addAll(currentPackages);
			onChange.report(null);
			Log.i(TAG, "Current app detected: " + lastApp);
		}
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		lastApp.clear();

		new RecurringRunner(new Runnable() {
			@Override
			public void run() {
				poll();
			}
		}, 333).ensureStarted();

		context.registerReceiver(systemKeyPressReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
	}

	private void poll() {
		String[] currentApps = currentApplicationRetriever.getPackages();
		if (currentApps.length > 0)
			setCurrentApp(currentApps);
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
