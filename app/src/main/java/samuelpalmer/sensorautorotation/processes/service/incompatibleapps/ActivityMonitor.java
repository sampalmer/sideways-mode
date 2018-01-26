package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.ComponentName;
import android.util.Log;

import samuelpalmer.sensorautorotation.utilities.ObjectExtensions;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {
	
	private static final String TAG = ActivityMonitor.class.getSimpleName();
	
	public final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
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
	
	@Override
	protected void start() {
		lastActivity = null;
	}

	@Override
	protected void stop() {
	}

}
