package samuelpalmer.sensorautorotation.notifications.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ServiceNotificationManager extends State {

	private final ServiceStatusFactory statusGenerator;

	public ServiceNotificationManager(ServiceStatusFactory statusGenerator) {
		this.statusGenerator = statusGenerator;
	}

	public final Set<ServiceStatusDisplay> onServiceStatusChanged = new HashSet<>();

	@Override
	protected void start() {
		updateNotification.update();

		for (EventRegistrar trigger : statusGenerator.updateTriggers())
			trigger.subscribe(updateNotification);
	}

	@Override
	protected void stop() {
		for (EventRegistrar trigger : statusGenerator.updateTriggers())
			trigger.unsubscribe(updateNotification);

		for (ServiceStatusDisplay display : onServiceStatusChanged)
			display.onServiceStopped();
	}

	private final EventHandler updateNotification = new EventHandler() {
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
