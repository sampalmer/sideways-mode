package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import android.content.ComponentName;
import android.util.Log;

import samuelpalmer.common.ObjectExtensions;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ActivityMonitor extends State {
	
	private static final String TAG = ActivityMonitor.class.getSimpleName();
	
	private final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private ComponentName lastActivity;

	public ComponentName currentOrNull() {
		return lastActivity;
	}

	public void setCurrentActivity(ComponentName activityOrNull) {
		if (!ObjectExtensions.areEqual(activityOrNull, lastActivity)) {
			Log.i(TAG, "Activity changed"); // Not logging specific activity since that would be a privacy violation since logs are included in error reports.
			lastActivity = activityOrNull;
			onChange.report();
		}
	}

	public EventRegistrar onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		lastActivity = null;
	}

	@Override
	protected void stop() {
	}

}
