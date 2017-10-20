package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.ApplicationStatus;
import samuelpalmer.sensorautorotation.State;

public class ApplicationStatusDisplayUpdater extends State {

	private final OrientationCalculator calculator;
	private final ApplicationStatusDisplay display;

	public ApplicationStatusDisplayUpdater(OrientationCalculator calculator) {
		this.calculator = calculator;
		this.display = new ApplicationStatusDisplays();
	}

	@Override
	protected void start() {
		updateDisplays.update();
		calculator.onAnythingChanged.subscribe(updateDisplays);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(updateDisplays);

		display.onApplicationStopped();
	}

	private final EventHandler updateDisplays = new EventHandler() {
		@Override
		public void update() {
			ApplicationStatus applicationStatus = new ApplicationStatus(
				null,
				true,
				calculator.side(),
				calculator.isWaitingForSensor()
			);

			if (lastApplicationStatus == null || !applicationStatus.equals(lastApplicationStatus)) {
				display.update(applicationStatus);
				lastApplicationStatus = applicationStatus;
			}
		}
	};

	public ApplicationStatus getLastStatus() {
		return lastApplicationStatus;
	}

	private ApplicationStatus lastApplicationStatus;
}
