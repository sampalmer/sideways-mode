package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.util.Log;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor extends State {

	private static final String TAG = ActivityMonitor.class.getSimpleName();

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private final Context context;
	private String lastApp;

	public ActivityMonitor(Context context) {
		this.context = context;
	}

	public String current() {
		return lastApp;
	}

	public void setCurrentApp(String currentApp) {
		if (!currentApp.equals(lastApp)) {
			lastApp = currentApp;
			onChange.report(null);
			Log.i(TAG, "Current app detected: " + lastApp);
//			Toast.makeText(context, lastApp, Toast.LENGTH_SHORT).show();
		}
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}

	@Override
	protected void start() {
		lastApp = "";
	}

	@Override
	protected void stop() {
	}

}
