package samuelpalmer.sensorautorotation.processes.service.userinteractivity;

public class UserInteractivity {

	private final boolean screenOn;

	public UserInteractivity(boolean screenOn) {
		this.screenOn = screenOn;
	}

	public boolean active() {
		return screenOn;
	}

}
