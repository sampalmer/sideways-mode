package samuelpalmer.sensorautorotation.orientationenforcement.currentapplicationretrieval;

import android.app.ActivityManager;

import java.lang.reflect.Field;

public class LollipopCurrentApplicationRetriever extends CurrentApplicationRetriever {

	private static final int PROCESS_STATE_TOP = 2;
	private final Field processStateField;

	LollipopCurrentApplicationRetriever(ActivityManager activityManager) {
		super(activityManager);

		try {
			processStateField = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	public String[] getPackages() {
		for (ActivityManager.RunningAppProcessInfo process : activityManager.getRunningAppProcesses())
			if (isTop(process))
			   /*
                 If multiple candidate processes can get here,
                 it's most likely that apps are being switched.
                 The first one provided by the OS seems to be
                 the one being switched to, so we stop here.
                */
				return getPackages(process);

		return new String[] { };
	}

	private String[] getPackages(ActivityManager.RunningAppProcessInfo process) {
		if (process.pkgList == null)
			return new String[]{};
		else
			/*
			 This can happen if multiple apps are sharing this process. From a quick test,
			 it looks like the order of the returned package list isn't related to which app
			 is actually on top. It looks like it isn't very common for this to happen. We're
			 just returning all of the packages since any of them could be the important one.
			 */
			return process.pkgList;
	}

	private boolean isTop(ActivityManager.RunningAppProcessInfo process) {
		return
				// Filters out most non-activity processes
				process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
				&&
				// Filters out processes that are just being
				// _used_ by the process with the activity
				process.importanceReasonCode == 0
				&&
				getState(process) == PROCESS_STATE_TOP;
	}

	private int getState(ActivityManager.RunningAppProcessInfo process) {
		try {
			return processStateField.getInt(process);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
