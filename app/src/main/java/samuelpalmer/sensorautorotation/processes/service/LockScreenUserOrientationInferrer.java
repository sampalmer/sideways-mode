package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class LockScreenUserOrientationInferrer extends State {

	private final LockScreenMonitor lockScreenMonitor;
	private final OrientationCalculator calculator;

	public LockScreenUserOrientationInferrer(LockScreenMonitor lockScreenMonitor, OrientationCalculator calculator) {
		this.lockScreenMonitor = lockScreenMonitor;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		lockScreenMonitor.subscribe(lockScreenChanged);
		update();
	}

	@Override
	protected void stop() {
		lockScreenMonitor.unsubscribe(lockScreenChanged);
	}

	private final EventHandler lockScreenChanged = new EventHandler() {
		@Override
		public void update() {
			LockScreenUserOrientationInferrer.this.update();
		}
	};

	private void update() {
		boolean onLockScreen = lockScreenMonitor.isOnLockScreen();
		if (!onLockScreen || ApplicationSettings.local().inferUserWhenLocked().get())
			calculator.setLockingOverride(onLockScreen);
	}

}
