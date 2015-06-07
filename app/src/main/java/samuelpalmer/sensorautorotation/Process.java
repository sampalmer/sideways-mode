package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class Process extends Application {

	public static final String TAG = Process.class.getSimpleName();
	private ApplicationSettings.Setting<Boolean> enabledPreference;
	private static Context context;

	public static Context context() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Process created");

		context = this;

		ensureVersionRecorded();
		serviceStateUpdater.update();
		enabledPreference = new ApplicationSettings().enabled();
		enabledPreference.onChange().subscribe(serviceStateUpdater);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		enabledPreference.onChange().unsubscribe(serviceStateUpdater);

		Log.i(TAG, "Process terminated");
	}

	private void ensureVersionRecorded() {
		ApplicationSettings.Setting<Integer> instructedVersion = new ApplicationSettings().lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			instructedVersion.setAndCommit(new VersionInfo().installedVersionCode());
	}

	private final EventHandler serviceStateUpdater = new EventHandler() {
		@Override
		public void update() {
			ServiceController.updateFromSettings();
		}
	};
}
