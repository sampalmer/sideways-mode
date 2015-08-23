package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public class ServiceNotificationManager extends State {

	private final NotificationServiceStatusDisplay display;
	private final ServiceStatusGenerator statusGenerator;

	public ServiceNotificationManager(ServiceStatusGenerator statusGenerator, NotificationServiceStatusDisplay display) {
		this.statusGenerator = statusGenerator;
		this.display = display;
	}

	@Override
	protected void start() {
		updateNotification.update();

		for (EventRegistrar trigger : display.updateTriggers())
			trigger.subscribe(updateNotification);
	}

	@Override
	protected void stop() {
		for (EventRegistrar trigger : display.updateTriggers())
			trigger.unsubscribe(updateNotification);
	}

	private final EventHandler updateNotification = new EventHandler() {
		@Override
		public void update() {
			NotificationParameters params = statusGenerator.generate();
			display.update(params);
		}
	};

}
