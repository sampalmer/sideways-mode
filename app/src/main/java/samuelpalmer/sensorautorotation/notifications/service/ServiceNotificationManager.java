package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final ServiceNotification serviceNotification;
	private final ScreenOrientationUpdater screen;
	private final LockScreenHider notificationHider;
	private final EventRegistrar<Void> onShowNotificationAtTopChanged;
	private final ScreenRotationMonitor screenRotationMonitor;

	public ServiceNotificationManager(ServiceNotification serviceNotification, ApplicationSettings settings, ScreenOrientationUpdater screen) {
		this.serviceNotification = serviceNotification;
		this.screen = screen;
		notificationHider = new LockScreenHider(updateNotification);
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
		screenRotationMonitor = new ScreenRotationMonitor();
	}

	@Override
	protected void start() {
		serviceNotification.set();
		notificationHider.ensureStarted();
		onShowNotificationAtTopChanged.subscribe(updateNotification);
		screen.onChange().subscribe(updateNotification);

		screenRotationMonitor.onChange().subscribe(updateNotification);
		screenRotationMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		screenRotationMonitor.ensureStopped();
		screenRotationMonitor.onChange().unsubscribe(updateNotification);

		screen.onChange().unsubscribe(updateNotification);
		onShowNotificationAtTopChanged.unsubscribe(updateNotification);
		notificationHider.ensureStopped();
	}

	private final EventHandler<Void> updateNotification = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			serviceNotification.set();
		}
	};

}
