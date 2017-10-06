package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Message;

import samuelpalmer.sensorautorotation.UserSide;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus {
	
	public final boolean appEnabled;
	public final UserSide user;
	public final boolean waitingForSensor;

	public ApplicationStatus(boolean appEnabled, UserSide user, boolean waitingForSensor) {
		this.appEnabled = appEnabled;
		this.user = user;
		this.waitingForSensor = waitingForSensor;
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			appEnabled == other.appEnabled
			&&
			user.equals(other.user)
			&&
			waitingForSensor == other.waitingForSensor;
	}
	
	private static final int MSG_WHAT = 1;
	
	public ApplicationStatus(Message message) {
		if (message.arg1 == 0)
			this.user = null;
		else if (message.arg1 == UserSide.LEFT.offset().clockwiseCount())
			this.user = UserSide.LEFT;
		else if (message.arg1 == UserSide.RIGHT.offset().clockwiseCount())
			this.user = UserSide.RIGHT;
		else
			throw new IllegalArgumentException("Unknown value for user side offset: " + message.arg1);
		
		waitingForSensor = (message.arg2 & 0x1) == 0x1;
		appEnabled = (message.arg2 & 0x2) == 0x2;
	}
	
	public Message asMessage() {
		return Message.obtain(
			null,
			MSG_WHAT,
			user == null ? 0 : user.offset().clockwiseCount(),
			(waitingForSensor ? 1 : 0)
			|
			(appEnabled ? 1 : 0) << 1
		);
	}
	
}
