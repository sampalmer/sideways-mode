package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Setting;

public class Process extends Application {

	private static final String TAG = Process.class.getSimpleName();
	private static Context context;
	private static Setting<Boolean> enabledPreference;

	public static Context context() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "Process created");

		context = this;

		enabledPreference = ApplicationSettings.enabled();
		enabledPreference.subscribe(serviceStateUpdater);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		enabledPreference.unsubscribe(serviceStateUpdater);

		Log.i(TAG, "Process terminated");
	}

	private static final EventHandler serviceStateUpdater = new EventHandler() {
		@Override
		public void update() {
			ServiceController.updateFromSettings();
		}
	};

}
