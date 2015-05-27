package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ThisApplication extends Application {

	private ApplicationSettings.Setting<Boolean> enabledPreference;
	private static Context context;

	public static Context context() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		context = this;

		ensureVersionRecorded();
		enabledPreference = new ApplicationSettings().enabled();

		enabledPreference.onChange().subscribe(serviceStateUpdater);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		enabledPreference.onChange().unsubscribe(serviceStateUpdater);
	}

	private void ensureVersionRecorded() {
		ApplicationSettings.Setting<Integer> instructedVersion = new ApplicationSettings().lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			instructedVersion.setAndCommit(new VersionInfo().installedVersionCode());
	}

	private final EventHandler serviceStateUpdater = new EventHandler() {
		@Override
		public void update() {
			new ServiceController().updateFromSettings();
		}
	};
}
