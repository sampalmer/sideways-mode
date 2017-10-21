package samuelpalmer.sensorautorotation.processes.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class ApplicationStatusDisplayUpdater extends State {

	private final OrientationCalculator calculator;
	private final Set<ApplicationStatusDisplay> displays;

	public ApplicationStatusDisplayUpdater(OrientationCalculator calculator, RotationService service) {
		this.calculator = calculator;
		displays = new HashSet<>();
		displays.add(new NotificationStatusDisplay(service));
		displays.add(new ApplicationStatusService.RemoteStatusDisplay());
	}

	@Override
	protected void start() {
		updateDisplays.update();
		calculator.onAnythingChanged.subscribe(updateDisplays);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(updateDisplays);
		
		for (ApplicationStatusDisplay display : displays)
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
				for (ApplicationStatusDisplay display : displays)
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
