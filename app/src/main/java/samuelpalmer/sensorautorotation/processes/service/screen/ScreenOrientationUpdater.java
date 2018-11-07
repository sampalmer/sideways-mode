package samuelpalmer.sensorautorotation.processes.service.screen;

import android.content.Context;
import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	
	public ScreenOrientationUpdater(Context context, OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor) {
		this.calculator = calculator;
		this.screen = new ScreenOrientationController(context, screenRotationMonitor);
	}

	@Override
	protected void start() {
		screen.ensureStarted();
		calculator.onAnythingChanged.subscribe(applyChange);
	}

	@Override
	protected void stop() {
		// Once the screen remained in the wrong orientation after turning the app off on my Motorola Moto G5 plus.
		// It seemed that system auto-rotation didn't kick in straight away, or maybe it was actually waiting for the next device rotation.
		// So we'll return the screen to the appropiate orientation for the user in the upright position before turning system auto-rotation back on.
		screen.propose(calculator.proposedScreenOrientationForUser(Orientation.NATURAL));
		
		calculator.onAnythingChanged.unsubscribe(applyChange);
		screen.ensureStopped();
	}
	
	private final EventHandler applyChange = this::propose;

	private void propose() {
		Orientation orientation = calculator.proposedScreenOrientation();
		
		if (!orientation.equals(lastProposed)) {
			lastProposed = orientation;
			screen.propose(orientation);
		}
	}
	
	@Nullable
	private Orientation lastProposed;
	
}
