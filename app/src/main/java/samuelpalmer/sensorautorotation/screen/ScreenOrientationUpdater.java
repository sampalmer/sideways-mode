package samuelpalmer.sensorautorotation.screen;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ScreenOrientationUpdater extends State {

	private final ApplicationSettings settings;
	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;

	public ScreenOrientationUpdater(ApplicationSettings settings, OrientationCalculator calculator, ScreenOrientationController screen) {
		this.settings = settings;
		this.calculator = calculator;
		this.screen = screen;
	}

	@Override
	protected void start() {
		calculator.onChange().subscribe(applyChange);
	}

	@Override
	protected void stop() {
		calculator.onChange().unsubscribe(applyChange);
	}

	private final EventHandler<Void> applyChange = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			if (!upsideDown() || settings.allowUpsideDownScreen().get())
				propose();
		}
	};

	private void propose() {
		screen.propose(calculator.proposedScreenOrientation());
		onProposedOrientationChanged.report(null);
	}

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

	private final UpdatingEventRegistrar<Void> onProposedOrientationChanged = new UpdatingEventRegistrar<>();

	public EventRegistrar<Void> onChange() {
		return onProposedOrientationChanged;
	}
}
