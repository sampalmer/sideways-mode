package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ApplicationStatusDisplayUpdater extends State {

	private final ApplicationStatusFactory statusGenerator;
	private final ApplicationStatusDisplay display;

	public ApplicationStatusDisplayUpdater(ApplicationStatusFactory statusGenerator, ApplicationStatusDisplay display) {
		this.statusGenerator = statusGenerator;
		this.display = display;
	}

	@Override
	protected void start() {
		updateDisplays.update();

		for (EventRegistrar trigger : statusGenerator.updateTriggers())
			trigger.subscribe(updateDisplays);
	}

	@Override
	protected void stop() {
		for (EventRegistrar trigger : statusGenerator.updateTriggers())
			trigger.unsubscribe(updateDisplays);

		display.onApplicationStopped();
	}

	private final EventHandler updateDisplays = new EventHandler() {
		@Override
		public void update() {
			ApplicationStatus applicationStatus = statusGenerator.generate();

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
