package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.pm.ActivityInfo;

import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ActivityMonitor extends State {

	private final UpdatingEventRegistrar onChange = new UpdatingEventRegistrar();
	private CurrentActivity last;

	public CurrentActivity currentOrNull() {
		return last;
	}

	public void setCurrentApp(ActivityInfo activity) {
		CurrentActivity newActivity = new CurrentActivity(activity);

		if (!newActivity.equals(last)) {
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
