package samuelpalmer.sensorautorotation.processes.ui;

import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

abstract class UiProcess {

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
		enabledPreference = new ApplicationSettings().enabled();
		enabledPreference.onChange().subscribe(serviceStateUpdater);
	}

	private static void ensureVersionRecorded() {
		ApplicationSettings.Setting<Integer> instructedVersion = new ApplicationSettings().lastVersionUserInstructedAbout();
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
