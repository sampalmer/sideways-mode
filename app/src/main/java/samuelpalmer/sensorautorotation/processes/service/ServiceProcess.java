package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

abstract class ServiceProcess {

	private static boolean initialised = false;

	/**
	 * Must be called at all entry points to this process
	 */
	public static void ensureInitialised() {
		if (!initialised) {
			initialise();
			initialised = true;
		}
	}

	private static ApplicationSettings.Setting<Boolean> enabledPreference;

	private static void initialise() {
		ensureVersionRecorded();
		serviceStateUpdater.update();
		enabledPreference = new ApplicationSettings(Prefs.prefs()).enabled();
		enabledPreference.onChange().subscribe(serviceStateUpdater);
	}

	private static void ensureVersionRecorded() {
		ApplicationSettings.Setting<Integer> instructedVersion = new ApplicationSettings(Prefs.prefs()).lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			instructedVersion.setAndCommit(new VersionInfo().installedVersionCode());
	}

	private static final EventHandler serviceStateUpdater = new EventHandler() {
		@Override
		public void update() {
			ServiceController.updateFromSettings();
		}
	};

}
