package samuelpalmer.sensorautorotation.processes.service.screen;

import android.support.annotation.Nullable;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	
	public ScreenOrientationUpdater(OrientationCalculator calculator, ScreenOrientationController screen) {
		this.calculator = calculator;
		this.screen = screen;
	}

	@Override
	protected void start() {
		calculator.onAnythingChanged.subscribe(applyChange);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(applyChange);
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
