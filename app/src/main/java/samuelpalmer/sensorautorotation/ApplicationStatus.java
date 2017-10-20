package samuelpalmer.sensorautorotation;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;

import samuelpalmer.common.ObjectExtensions;
import samuelpalmer.sensorautorotation.errors.EnvironmentError;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus {
	
	@Nullable
	public final EnvironmentError error;
	public final boolean appEnabled;
	public final UserSide user;
	public final boolean waitingForSensor;

	public ApplicationStatus(@Nullable EnvironmentError error, boolean appEnabled, UserSide user, boolean waitingForSensor) {
		this.error = error;
		this.appEnabled = appEnabled;
		this.user = user;
		this.waitingForSensor = waitingForSensor;
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			ObjectExtensions.areEqual(error, other.error)
			&&
			appEnabled == other.appEnabled
			&&
			ObjectExtensions.areEqual(user, other.user)
			&&
			waitingForSensor == other.waitingForSensor;
	}
	
	private static final int MSG_WHAT = 1;
	
	public ApplicationStatus(Message message) {
		if (message.arg1 == 0)
			this.user = null;
		else if (message.arg1 == UserSide.LEFT.orientation.offset)
			this.user = UserSide.LEFT;
		else if (message.arg1 == UserSide.RIGHT.orientation.offset)
			this.user = UserSide.RIGHT;
		else
			throw new IllegalArgumentException("Unknown value for user side offset: " + message.arg1);
		
		waitingForSensor = (message.arg2 & 0x1) == 0x1;
		appEnabled = (message.arg2 & 0x2) == 0x2;
		
		String errorJson = message.getData().getString(null);
		if (errorJson != null)
			error = EnvironmentError.fromJson(errorJson);
		else
			error = null;
	}
	
	public Message asMessage() {
		Message result = Message.obtain(
			null,
			MSG_WHAT,
			user == null ? 0 : user.orientation.offset,
			(waitingForSensor ? 1 : 0)
			|
			(appEnabled ? 1 : 0) << 1
		);
		
		if (error != null) {
			Bundle bundle = new Bundle();
			bundle.putString(null, error.toJson());
			result.setData(bundle);
		}
		
		return result;
	}
	
}
