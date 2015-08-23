package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class ServiceNotificationManager extends State {

	private final NotificationServiceStatusDisplay display;
	private final ScreenOrientationUpdater screen;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final ServiceStatusGenerator statusGenerator;

	public ServiceNotificationManager(ServiceStatusGenerator statusGenerator, NotificationServiceStatusDisplay display, ScreenOrientationUpdater screen) {
		this.statusGenerator = statusGenerator;
		this.display = display;
		this.screen = screen;
		screenRotationMonitor = new ScreenRotationMonitor();
	}

	@Override
	protected void start() {
		updateNotification.update();

		for (EventRegistrar trigger : display.updateTriggers())
			trigger.subscribe(updateNotification);

		screen.onProposedOrientationChanged().subscribe(updateNotification);
		screenRotationMonitor.subscribe(updateNotification);
	}

	@Override
	protected void stop() {
		screenRotationMonitor.unsubscribe(updateNotification);
		screen.onProposedOrientationChanged().unsubscribe(updateNotification);

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
