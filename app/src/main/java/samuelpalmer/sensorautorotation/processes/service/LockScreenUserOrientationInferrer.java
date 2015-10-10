package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;

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
		calculator.setLockingOverride(false);
	}

	private final EventHandler lockScreenChanged = new EventHandler() {
		@Override
		public void update() {
			LockScreenUserOrientationInferrer.this.update();
		}
	};

	private void update() {
		calculator.setLockingOverride(lockScreenMonitor.isOnLockScreen());
	}

}
