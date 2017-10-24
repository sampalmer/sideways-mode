package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class ApplicationStatusDisplayUpdater extends State {

	private final OrientationCalculator calculator;
	private final RotationService service;
	private final ServiceNotification notificationBuilder;
	
	public ApplicationStatusDisplayUpdater(OrientationCalculator calculator, RotationService service) {
		this.calculator = calculator;
		this.service = service;
		notificationBuilder = new ServiceNotification();
	}

	@Override
	protected void start() {
		updateDisplays.update();
		calculator.onAnythingChanged.subscribe(updateDisplays);
	}

	@Override
	protected void stop() {
		calculator.onAnythingChanged.unsubscribe(updateDisplays);
	}

	private final EventHandler updateDisplays = new EventHandler() {
		@Override
		public void update() {
			ApplicationStatus applicationStatus = new ApplicationStatus(
				new ApplicationStatus.RunningState(calculator.side(), calculator.isWaitingForSensor()),
				null
			);

			if (lastApplicationStatus == null || !applicationStatus.equals(lastApplicationStatus)) {
				Notification notification = notificationBuilder.build(applicationStatus.runningState);
				
				// Running the service in the foreground reduces the chance of the process being killed.
				// This is important, since the process being killed would result in the screen no longer rotating.
				service.startForeground(NotificationIds.SERVICE, notification);
				ApplicationStatusService.sendUpdate(applicationStatus);
				
				lastApplicationStatus = applicationStatus;
			}
		}
	};

	public ApplicationStatus getLastStatus() {
		return lastApplicationStatus;
	}

	private ApplicationStatus lastApplicationStatus;
}
