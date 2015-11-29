package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class LockScreenUserOrientationInferenceManager extends State {

	private final LockScreenUserOrientationInferrer inferrer;
	private final Setting<Boolean> setting = ApplicationSettings.local().inferUserWhenLocked();

	public LockScreenUserOrientationInferenceManager(LockScreenUserOrientationInferrer inferrer) {
		this.inferrer = inferrer;
	}

	@Override
	protected void start() {
		refresh();
		setting.subscribe(refresh);
	}

	@Override
	protected void stop() {
		setting.unsubscribe(refresh);
		inferrer.ensureStopped();
	}

	private final EventHandler refresh = new EventHandler() {
		@Override
		public void update() {
			refresh();
		}
	};

	private void refresh() {
		if (setting.get())
			inferrer.ensureStarted();
		else
			inferrer.ensureStopped();
	}
}
