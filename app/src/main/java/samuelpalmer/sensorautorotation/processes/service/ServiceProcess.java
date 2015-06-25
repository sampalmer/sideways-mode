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

		//Since the service might have stopped unexpectedly without this process being killed.
		//For example, the user might've explicitly stopped the service in the Android app manager.
		serviceStateUpdater.update();
	}

	private static ApplicationSettings.Setting<Boolean> enabledPreference;

	private static void initialise() {
		ensureVersionRecorded();
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
