package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;

public class LockScreenUserOrientationInferenceManager extends State {

	private final LockScreenUserOrientationInferrer inferrer;
	private final Setting<Boolean> setting = ApplicationSettings.local().inferUserWhenLocked();

	public LockScreenUserOrientationInferenceManager(LockScreenUserOrientationInferrer inferrer) {
		this.inferrer = inferrer;
	}

	@Override
	protected void start() {
		setting.subscribe(refresh);
		refresh();
	}

	@Override
	protected void stop() {
		setting.unsubscribe(refresh);
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
