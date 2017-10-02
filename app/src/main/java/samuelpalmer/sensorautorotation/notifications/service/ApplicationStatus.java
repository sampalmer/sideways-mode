package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Message;
import android.os.Parcel;

import samuelpalmer.sensorautorotation.UserSide;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus {
	
	public final UserSide user;
	public final boolean waitingForSensor;

	public ApplicationStatus(UserSide user, boolean waitingForSensor) {
		this.user = user;
		this.waitingForSensor = waitingForSensor;
	}

	private ApplicationStatus(Parcel in) {
		user = UserSide.valueOf(in.readString());
		waitingForSensor = in.readByte() != 0;
		
		if (in.dataAvail() != 0)
			throw new RuntimeException("Found more data in parcel than expected");
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			user.equals(other.user)
			&&
			waitingForSensor == other.waitingForSensor;
	}
	
	private static final int MSG_WHAT = 1;
	
	public ApplicationStatus(Message message) {
		if (message.arg1 == UserSide.LEFT.offset().clockwiseCount())
			this.user = UserSide.LEFT;
		else if (message.arg1 == UserSide.RIGHT.offset().clockwiseCount())
			this.user = UserSide.RIGHT;
		else
			throw new IllegalArgumentException("Unknown value for user side offset: " + message.arg1);
		
		if (message.arg2 == 0)
			this.waitingForSensor = false;
		else if (message.arg2 == 1)
			this.waitingForSensor = true;
		else
			throw new IllegalArgumentException("Unknown value for waitingForSensor: " + message.arg2);
	}
	
	public Message asMessage() {
		return Message.obtain(null, MSG_WHAT, user.offset().clockwiseCount(), waitingForSensor ? 1 : 0);
	}
}
