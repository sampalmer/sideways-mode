package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class ScreenOrientationUpdater extends State {

	private final Settings settings;
	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;

	public ScreenOrientationUpdater(Settings settings, OrientationCalculator calculator, ScreenOrientationController screen) {
		this.settings = settings;
		this.calculator = calculator;
		this.screen = screen;
	}

	@Override
	protected void start() {
		calculator.onChange().subscribe(subscriber);
	}

	@Override
	protected void stop() {
		calculator.onChange().unsubscribe(subscriber);
	}

	private final EventHandler<Void> subscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			if (!upsideDown() || settings.allowUpsideDownScreen().get())
				screen.propose(calculator.proposedScreenOrientation());
		}
	};

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

}
