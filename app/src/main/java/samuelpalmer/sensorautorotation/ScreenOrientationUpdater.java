package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private ScreenOrientationController screen;

	public ScreenOrientationUpdater(OrientationCalculator calculator, ScreenOrientationController screen) {
		this.calculator = calculator;
		this.screen = screen;
	}

	@Override
	protected void start() throws EnvironmentException {
		calculator.onChange().subscribe(subscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		calculator.onChange().unsubscribe(subscriber);
	}

	private final EventHandler<Void> subscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) throws EnvironmentException {
			if (!upsideDown())
				screen.propose(calculator.proposedScreenOrientation());
		}
	};

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

}
