package samuelpalmer.sensorautorotation.processes.service.screen;

import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	
	public ScreenOrientationUpdater(OrientationCalculator calculator, SystemSettings systemSettings, ScreenRotationMonitor screenRotationMonitor) {
		this.calculator = calculator;
		this.screen = new ScreenOrientationController(systemSettings, screenRotationMonitor);
	}

	@Override
	protected void start() {
		screen.ensureStarted();
		calculator.onAnythingChanged.subscribe(applyChange);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(applyChange);
		screen.ensureStopped();
	}

	private final EventHandler applyChange = new EventHandler() {
		@Override
		public void update() {
			propose();
		}
	};

	private void propose() {
		Orientation orientation = calculator.proposedScreenOrientation();
		
		if (lastProposed == null || !orientation.equals(lastProposed)) {
			lastProposed = orientation;
			screen.propose(orientation);
		}
	}
	
	@Nullable
	private Orientation lastProposed;
	
}
