package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.pm.ActivityInfo;
import android.util.Log;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {

	private static final String TAG = ActivityMonitor.class.getSimpleName();

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private CurrentActivity last;

	public CurrentActivity current() {
		return last;
	}

	public void setCurrentApp(ActivityInfo activity) {
		CurrentActivity newActivity = new CurrentActivity(activity);

		if (!newActivity.equals(last)) {
			last = newActivity;
			onChange.report(null);
			Log.i(TAG, "Current activity detected: " + last);
//			Toast.makeText(context, last, Toast.LENGTH_SHORT).show();
		}
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		last = new CurrentActivity();
	}

	@Override
	protected void stop() {
	}

}
