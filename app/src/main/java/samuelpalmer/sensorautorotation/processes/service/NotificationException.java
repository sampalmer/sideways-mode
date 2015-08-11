package samuelpalmer.sensorautorotation.processes.service;

public class NotificationException extends RuntimeException {

	public NotificationException(Exception cause) {
		super(cause);
	}

	public NotificationException(String message) {
		super(message);
	}

}
