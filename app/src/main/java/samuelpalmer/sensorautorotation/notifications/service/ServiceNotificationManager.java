package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public class ServiceNotificationManager extends State {

	private final NotificationUpdater display;
	private final NotificationParametersFactory statusGenerator;

	public ServiceNotificationManager(NotificationParametersFactory statusGenerator, NotificationUpdater display) {
		this.statusGenerator = statusGenerator;
		this.display = display;
	}

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
			display.update(params);
		}
	};

}
