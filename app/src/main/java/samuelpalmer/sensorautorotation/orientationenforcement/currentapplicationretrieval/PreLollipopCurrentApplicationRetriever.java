package samuelpalmer.sensorautorotation.orientationenforcement.currentapplicationretrieval;

import android.app.ActivityManager;
import android.content.ComponentName;

import java.util.List;

public class PreLollipopCurrentApplicationRetriever extends CurrentApplicationRetriever {

	PreLollipopCurrentApplicationRetriever(ActivityManager activityManager) {
		super(activityManager);
	}

	@Override
	public String[] getPackages() {
		@SuppressWarnings("deprecation")
		List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
		ActivityManager.RunningTaskInfo currentTask = tasks.get(0);
		ComponentName currentActivity = currentTask.topActivity;
		return new String[] { currentActivity.getPackageName() };
	}

}
