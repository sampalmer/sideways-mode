package samuelpalmer.sensorautorotation.processes.service.screen;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ScreenOrientationUpdater extends State {

	private final OrientationCalculator calculator;
	private final ScreenOrientationController screen;
	private final ApplicationSettings.Setting<Boolean> allowUpsideDownScreen;

	public ScreenOrientationUpdater(ApplicationSettings settings, OrientationCalculator calculator, ScreenOrientationController screen) {
		this.calculator = calculator;
		this.screen = screen;
		allowUpsideDownScreen = settings.allowUpsideDownScreen();
	}

	@Override
	protected void start() {
		calculator.onChange().subscribe(applyChange);
		allowUpsideDownScreen.subscribe(applyChange);
	}

	@Override
	protected void stop() {
		allowUpsideDownScreen.unsubscribe(applyChange);
		calculator.onChange().unsubscribe(applyChange);
	}

	private final EventHandler applyChange = new EventHandler() {
		@Override
		public void update() {
			refresh();
		}
	};

	private void refresh() {
		if (!upsideDown() || allowUpsideDownScreen.get())
			propose();
	}

	private void propose() {
		screen.propose(calculator.proposedScreenOrientation());
		onProposedOrientationChanged.report();
	}

	private boolean upsideDown() {
		return calculator.proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN);
	}

	private final UpdatingEventRegistrar onProposedOrientationChanged = new UpdatingEventRegistrar();

	public EventRegistrar onProposedOrientationChanged() {
		return onProposedOrientationChanged;
	}
}
