package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.notifications.service.ServiceStatusGenerator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class LockScreenUserOrientationInferrer extends State {

	private final LockScreenMonitor lockScreenMonitor;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final OrientationCalculator calculator;
	private final ServiceStatusGenerator serviceStatusGenerator;
	private final ScreenOrientationUpdater screenOrientationUpdater;

	public LockScreenUserOrientationInferrer(LockScreenMonitor lockScreenMonitor, ScreenRotationMonitor screenRotationMonitor, OrientationCalculator calculator, ServiceStatusGenerator serviceStatusGenerator, ScreenOrientationUpdater screenOrientationUpdater) {
		this.lockScreenMonitor = lockScreenMonitor;
		this.screenRotationMonitor = screenRotationMonitor;
		this.calculator = calculator;
		this.serviceStatusGenerator = serviceStatusGenerator;
		this.screenOrientationUpdater = screenOrientationUpdater;
	}

	@Override
	protected void start() {
		lockScreenMonitor.subscribe(lockScreenChanged);
		screenOrientationUpdater.onProposedOrientationChanged().subscribe(proposedOrientationChanged);
	}

	@Override
	protected void stop() {
		screenOrientationUpdater.onProposedOrientationChanged().unsubscribe(proposedOrientationChanged);
		lockScreenMonitor.unsubscribe(lockScreenChanged);
	}

	private final EventHandler lockScreenChanged = new EventHandler() {
		@Override
		public void update() {
			onLockScreen = LockScreenMonitor.isOnLockScreen();
		}
	};

	private Boolean onLockScreen;

	private final EventHandler proposedOrientationChanged = new EventHandler() {
		@Override
		public void update() {
			if (onLockScreen != null && onLockScreen && serviceStatusGenerator.overridden())
				calculator.inferUser(screenRotationMonitor.currentRotation());
		}
	};

}
