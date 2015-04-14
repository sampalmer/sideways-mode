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
	private RecurringRunner recurringRunner;
	private final UpdatingEventRegistrar<Set<AppPackage>> onChange = new UpdatingEventRegistrar<>();

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
			onChange.report(getCurrentPackages());
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
		ActivityManager.RunningTaskInfo task = tasks.get(0);
		ComponentName mainActivity = task.baseActivity;
		result.add(new AppPackage(mainActivity.getPackageName()));
	}

	private void getCurrentPackagesLollipop(HashSet<AppPackage> result) {
		final int PROCESS_STATE_TOP = 2;

		try {
			Field processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");

			List<ActivityManager.RunningAppProcessInfo> processes = activityManager().getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo process : processes) {
				if (
					process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND //Filters out most non-activity processes
					&&
					process.importanceReasonCode == 0 //Filters out processes that are _used_ by the process with the activity
				) {
					int state = processStateField.getInt(process);

					if (state == PROCESS_STATE_TOP) {
						for (String processPackage : process.pkgList)
							result.add(new AppPackage(processPackage));
						return; //If multiple candidates are found, it's most likely that apps are being switched. The first one provided by the OS seems to be the one being switched to.
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
