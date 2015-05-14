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
				return process.pkgList;

		return new String[] { };
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
