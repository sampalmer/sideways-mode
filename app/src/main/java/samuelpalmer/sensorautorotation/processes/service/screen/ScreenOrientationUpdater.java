package samuelpalmer.sensorautorotation.processes.service.screen;

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
		if (lastProposed == null || !proposedOrientation().equals(lastProposed)) {
			lastProposed = proposedOrientation();
			screen.propose(proposedOrientation());
		}
	}

	public Orientation proposedOrientation() {
		if (!upsideDown() || allowUpsideDownScreen.get())
			return calculator.proposedScreenOrientation();
		else if (lastProposed != null)
			return lastProposed;
		else
			return Orientation.NATURAL;
	}

	private Orientation lastProposed;

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

}
