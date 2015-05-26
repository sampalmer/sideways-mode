package samuelpalmer.sensorautorotation.screen;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	private final ApplicationSettings.Setting<Boolean> allowUpsideDownScreen;

	public ScreenOrientationUpdater(ApplicationSettings settings, OrientationCalculator calculator, ScreenOrientationController screen) {
		this.calculator = calculator;
		this.screen = screen;
		allowUpsideDownScreen = settings.allowUpsideDownScreen;
	}

	@Override
	protected void start() {
		calculator.onChange().subscribe(applyChange);
		allowUpsideDownScreen.onChange().subscribe(applyChange);
	}

	@Override
	protected void stop() {
		allowUpsideDownScreen.onChange().unsubscribe(applyChange);
		calculator.onChange().unsubscribe(applyChange);
	}

	private final EventHandler<Void> applyChange = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			refresh();
		}
	};

	private void refresh() {
		if (!upsideDown() || allowUpsideDownScreen.get())
			propose();
	}

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
