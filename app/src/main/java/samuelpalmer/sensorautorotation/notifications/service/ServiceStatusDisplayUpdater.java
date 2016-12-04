package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ServiceStatusDisplayUpdater extends State {

	private final ServiceStatusFactory statusGenerator;
	private final ServiceStatusDisplay display;

	public ServiceStatusDisplayUpdater(ServiceStatusFactory statusGenerator, ServiceStatusDisplay display) {
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

		display.onServiceStopped();
	}

	private final EventHandler updateDisplays = new EventHandler() {
		@Override
		public void update() {
			ServiceStatus serviceStatus = statusGenerator.generate();

			if (lastServiceStatus == null || !serviceStatus.equals(lastServiceStatus)) {
				display.update(serviceStatus);
				lastServiceStatus = serviceStatus;
			}
		}
	};

	public ServiceStatus getLastStatus() {
		return lastServiceStatus;
	}

	private ServiceStatus lastServiceStatus;
}
