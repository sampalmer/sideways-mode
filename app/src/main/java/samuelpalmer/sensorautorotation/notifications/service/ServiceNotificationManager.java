package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final ScreenOrientationUpdater screen;
	private final LockScreenHider notificationHider;
	private final EventRegistrar onPriorityChanged;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final ServiceNotificationUpdater notificationUpdater;

	public ServiceNotificationManager(ServiceNotificationUpdater notificationUpdater, ApplicationSettings settings, ScreenOrientationUpdater screen) {
		this.notificationUpdater = notificationUpdater;
		this.screen = screen;
		notificationHider = new LockScreenHider(updateNotification);
		onPriorityChanged = settings.notificationPriority().onChange();
		screenRotationMonitor = new ScreenRotationMonitor();
	}

	@Override
	protected void start() {
		notificationUpdater.update();

		notificationHider.ensureStarted();
		onPriorityChanged.subscribe(updateNotification);
		screen.onProposedOrientationChanged().subscribe(updateNotification);

		screenRotationMonitor.onChange().subscribe(updateNotification);
		screenRotationMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		screenRotationMonitor.ensureStopped();
		screenRotationMonitor.onChange().unsubscribe(updateNotification);

		screen.onProposedOrientationChanged().unsubscribe(updateNotification);
		onPriorityChanged.unsubscribe(updateNotification);
		notificationHider.ensureStopped();

		notificationUpdater.cancel();
	}

	private final EventHandler updateNotification = new EventHandler() {
		@Override
		public void update() {
			notificationUpdater.update();
		}
	};

}
