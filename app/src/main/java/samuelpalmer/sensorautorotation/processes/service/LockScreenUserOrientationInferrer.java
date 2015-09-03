package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class LockScreenUserOrientationInferrer extends State {

	private final LockScreenMonitor lockScreenMonitor;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final OrientationCalculator calculator;

	public LockScreenUserOrientationInferrer(LockScreenMonitor lockScreenMonitor, ScreenRotationMonitor screenRotationMonitor, OrientationCalculator calculator) {
		this.lockScreenMonitor = lockScreenMonitor;
		this.screenRotationMonitor = screenRotationMonitor;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		lockScreenMonitor.subscribe(lockScreenChangeHandler);
	}

	@Override
	protected void stop() {
		lockScreenMonitor.unsubscribe(lockScreenChangeHandler);
		if (isMonitoring())
			stopMonitoring();
	}

	private Orientation lockScreenOrientation;

	private final EventHandler lockScreenChangeHandler = new EventHandler() {
		@Override
		public void update() {
			boolean isOnLockScreen = LockScreenMonitor.isOnLockScreen();
			if (isOnLockScreen != isMonitoring()) {
				if (isOnLockScreen) {
					screenRotationMonitor.subscribe(screenRotationChanged);
					cacheScreenOrientation();
				}
				else {
					calculator.inferUser(lockScreenOrientation);
					stopMonitoring();
				}
			}
		}
	};

	private void stopMonitoring() {
		lockScreenOrientation = null;
		screenRotationMonitor.unsubscribe(screenRotationChanged);
	}

	private boolean isMonitoring() {
		return lockScreenOrientation != null;
	}

	private final EventHandler screenRotationChanged = new EventHandler() {
		@Override
		public void update() {
			cacheScreenOrientation();
		}
	};

	private void cacheScreenOrientation() {
		lockScreenOrientation = screenRotationMonitor.currentRotation();
	}
}
