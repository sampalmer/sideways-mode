package samuelpalmer.sensorautorotation.unused;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

import samuelpalmer.sensorautorotation.RecurringRunner;

//TODO: Pause this on screen off
//TODO: Figure out how to prevent apps that require enforcement from launching in their own orientations
public class OrientationEnforcingMonitor {

	private final Context context;
	private ScreenOrientationEnforcer enforcer; //Must be accessed by UI thread.
	private RecurringRunner recurringRunner;

	public OrientationEnforcingMonitor(Context context) {
		this.context = context;

		//Alternatively, use a Timer to run in a different thread.
		recurringRunner = new RecurringRunner(updateEnforcement, 333);
	}

	public void start() {
		recurringRunner.start();
	}

	public void stop() {
		recurringRunner.stop();
	}

	private final Runnable updateEnforcement = new Runnable() {

		@Override
		public void run() {
			if (shouldEnforce())
				enforce();
			else
				dontEnforce();
		}

	};

	private boolean shouldEnforce() {
		final String packageName = getCurrentActivityPackageName();
		return packageName.equals(getHomePackage());
	}

	private String getHomePackage() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return resolveInfo.activityInfo.packageName;
	}

	private void enforce() {
		if (enforcer == null)
			enforcer = new ScreenOrientationEnforcer(context);
	}

	private void dontEnforce() {
		if (enforcer != null) {
			enforcer.close();
			enforcer = null;
		}
	}

	/**
	 * Only works before Android 5
	 */
	private String getCurrentActivityPackageName() {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
		RunningTaskInfo task = tasks.get(0);
		ComponentName mainActivity = task.baseActivity;
		return mainActivity.getPackageName();
	}

}
