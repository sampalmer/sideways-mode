package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final EventRegistrar<Void> onShowNotificationAtTopChanged;
	private final ServiceNotification serviceNotification;

	public ServiceNotificationManager(ServiceNotification serviceNotification, ApplicationSettings settings) {
		this.serviceNotification = serviceNotification;
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
	}

	@Override
	protected void start() {
		serviceNotification.set();
		onShowNotificationAtTopChanged.subscribe(updateNotification);
	}

	@Override
	protected void stop() {
		onShowNotificationAtTopChanged.unsubscribe(updateNotification);
	}

	private final EventHandler<Void> updateNotification = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			serviceNotification.set();
		}
	};

}
