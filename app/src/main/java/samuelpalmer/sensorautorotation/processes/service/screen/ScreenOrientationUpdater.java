package samuelpalmer.sensorautorotation.processes.service.screen;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	private final ApplicationSettings.Setting<Boolean> allowUpsideDownScreen;

	public ScreenOrientationUpdater(OrientationCalculator calculator, ScreenOrientationController screen) {
		this.calculator = calculator;
		this.screen = screen;
		allowUpsideDownScreen = ApplicationSettings.local().allowUpsideDownScreen();
	}

	@Override
	protected void start() {
		calculator.onAnythingChanged.subscribe(applyChange);
		allowUpsideDownScreen.subscribe(applyChange);
	}

	@Override
	protected void stop() {
		allowUpsideDownScreen.unsubscribe(applyChange);
		calculator.onAnythingChanged.unsubscribe(applyChange);
	}

	private final EventHandler applyChange = new EventHandler() {
		@Override
		public void update() {
			propose();
		}
	};

	private void propose() {
		Orientation orientation = proposedOrientation();
		
		if (orientation != null && (lastProposed == null || !orientation.equals(lastProposed))) {
			lastProposed = orientation;
			screen.propose(orientation);
		}
	}

	public @Nullable Orientation proposedOrientation() {
		Orientation proposed = calculator.proposedScreenOrientation();
		if (proposed != null && (!proposed.equals(Orientation.UPSIDE_DOWN) || allowUpsideDownScreen.get()))
			return proposed;
		else
			return lastProposed;
	}
	
	@Nullable
	private Orientation lastProposed;
	
}
