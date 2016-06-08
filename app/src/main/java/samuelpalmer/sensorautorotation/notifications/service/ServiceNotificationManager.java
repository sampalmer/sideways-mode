package samuelpalmer.sensorautorotation.notifications.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.State;

public class ServiceNotificationManager extends State {

	private final NotificationParametersFactory statusGenerator;

	public ServiceNotificationManager(NotificationParametersFactory statusGenerator) {
		this.statusGenerator = statusGenerator;
	}

	public final Set<ServiceStatusDisplay> onParamsChanged = new HashSet<>();

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
	}

	private final EventHandler updateNotification = new EventHandler() {
		@Override
		public void update() {
			NotificationParameters params = statusGenerator.generate();

			if (lastNotificationParams == null || !params.equals(lastNotificationParams)) {
				for (ServiceStatusDisplay display : onParamsChanged)
					display.update(params);

				lastNotificationParams = params;
			}
		}
	};

	private NotificationParameters lastNotificationParams;
}
