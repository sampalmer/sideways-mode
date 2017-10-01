package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import android.content.ComponentName;

import samuelpalmer.common.contentprovidersharedpreferences.ObjectExtensions;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ActivityMonitor extends State {

	private final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private ComponentName lastActivity;

	public ComponentName currentOrNull() {
		return lastActivity;
	}

	public void setCurrentActivity(ComponentName activityOrNull) {
		if (!ObjectExtensions.areEqual(activityOrNull, lastActivity)) {
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
