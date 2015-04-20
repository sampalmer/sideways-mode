package samuelpalmer.sensorautorotation.orientationenforcement;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {

	private final Context context;
	private final RecurringRunner recurringRunner;
	private final UpdatingEventRegistrar<Set<AppPackage>> onChange = new UpdatingEventRegistrar<>();
	private Set<AppPackage> lastPackages = new HashSet<>(0);

	public ActivityMonitor(Context context) {
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
			Set<AppPackage> currentPackages = getCurrentPackages();
			if (!currentPackages.equals(lastPackages)) {
				lastPackages = currentPackages;
				onChange.report(currentPackages);
			}
		}

	};

	private Set<AppPackage> getCurrentPackages() {
		HashSet<AppPackage> result = new HashSet<>();

		if (Build.VERSION.SDK_INT < 21)
			getCurrentPackagesPreLollipop(result);
		else
			getCurrentPackagesLollipop(result);

		return result;
	}

	private void getCurrentPackagesPreLollipop(HashSet<AppPackage> result) {
		@SuppressWarnings("deprecation")
		List<ActivityManager.RunningTaskInfo> tasks = activityManager().getRunningTasks(1);
		ActivityManager.RunningTaskInfo currentTask = tasks.get(0);
		ComponentName currentActivity = currentTask.topActivity;
		result.add(new AppPackage(currentActivity.getPackageName()));
	}

	private void getCurrentPackagesLollipop(HashSet<AppPackage> result) {
		final int PROCESS_STATE_TOP = 2;

		try {
			Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");

			List<ActivityManager.RunningAppProcessInfo> processes = activityManager().getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo process : processes) {
				if (
					// Filters out most non-activity processes
					process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
					&&
					// Filters out processes that are just being
					// _used_ by the process with the activity
					process.importanceReasonCode == 0
				) {
					int state = processStateField.getInt(process);

					if (state == PROCESS_STATE_TOP) {
						for (String processPackage : process.pkgList)
							result.add(new AppPackage(processPackage));
						/*
						 If multiple candidate processes can get here,
						 it's most likely that apps are being switched.
						 The first one provided by the OS seems to be
						 the one being switched to, so we stop here.
						 */
						return;
					}
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private ActivityManager activityManager() {
		return (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	}

	public EventRegistrar<Set<AppPackage>> onChange() {
		return onChange;
	}
}
