package samuelpalmer.sensorautorotation;

import android.app.Application;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ThisApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ensureVersionRecorded();
	}

	private void ensureVersionRecorded() {
		ApplicationSettings.Setting<Integer> instructedVersion = new ApplicationSettings(this).lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			instructedVersion.setAndCommit(new VersionInfo(this).installedVersionCode());
	}
}
