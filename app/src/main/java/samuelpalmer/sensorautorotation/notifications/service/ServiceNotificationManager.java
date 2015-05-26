package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceNotificationManager extends State {

	private final ScreenOrientationUpdater screen;
	private final LockScreenHider notificationHider;
	private final EventRegistrar<Void> onShowNotificationAtTopChanged;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final ServiceNotificationUpdater notificationUpdater;

	public ServiceNotificationManager(ServiceNotificationUpdater notificationUpdater, ApplicationSettings settings, ScreenOrientationUpdater screen) {
		this.notificationUpdater = notificationUpdater;
		this.screen = screen;
		notificationHider = new LockScreenHider(updateNotification);
		onShowNotificationAtTopChanged = settings.showNotificationAtTop().onChange();
		screenRotationMonitor = new ScreenRotationMonitor();
	}

	@Override
	protected void start() {
		notificationUpdater.update();
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
			notificationUpdater.update();
		}
	};

}
