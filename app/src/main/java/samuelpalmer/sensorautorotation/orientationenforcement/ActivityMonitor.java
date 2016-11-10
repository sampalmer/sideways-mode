package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.pm.ActivityInfo;

import samuelpalmer.common.contentprovidersharedpreferences.ObjectExtensions;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ActivityMonitor extends State {

	private final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private CurrentActivity last;

	public CurrentActivity currentOrNull() {
		return last;
	}

	public void setCurrentApp(ActivityInfo activityOrNull) {
		CurrentActivity newActivity = activityOrNull == null ? null : new CurrentActivity(activityOrNull);

		if (!ObjectExtensions.areEqual(newActivity, last)) {
			last = newActivity;
			onChange.report();
		}
	}

	public EventRegistrar onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		last = null;
	}

	@Override
	protected void stop() {
	}

}
