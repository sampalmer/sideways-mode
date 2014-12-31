package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class ScreenOrientationUpdater extends State {

	private final Context context;
	private final OrientationCalculator calculator;
	private ScreenOrientationController screen;

	public ScreenOrientationUpdater(Context context, OrientationCalculator calculator, ScreenOrientationController screen) {
		this.context = context;
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
			if (!upsideDown() || new Settings(context).allowUpsideDownScreen().getOr(false))
				screen.propose(calculator.proposedScreenOrientation());
		}
	};

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

}
