package samuelpalmer.sensorautorotation.notifications.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ServiceStatusDisplayManager extends State {

	private final ServiceStatusFactory statusGenerator;

	public ServiceStatusDisplayManager(ServiceStatusFactory statusGenerator) {
		this.statusGenerator = statusGenerator;
	}

	public final Set<ServiceStatusDisplay> onServiceStatusChanged = new HashSet<>();

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

		for (ServiceStatusDisplay display : onServiceStatusChanged)
			display.onServiceStopped();
	}

	private final EventHandler updateDisplays = new EventHandler() {
		@Override
		public void update() {
			ServiceStatus serviceStatus = statusGenerator.generate();

			if (lastServiceStatus == null || !serviceStatus.equals(lastServiceStatus)) {
				for (ServiceStatusDisplay display : onServiceStatusChanged)
					display.update(serviceStatus);

				lastServiceStatus = serviceStatus;
			}
		}
	};

	private ServiceStatus lastServiceStatus;
}
