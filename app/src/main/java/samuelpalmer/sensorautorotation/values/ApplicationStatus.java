package samuelpalmer.sensorautorotation.values;

import android.os.Bundle;
import android.os.Message;
import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.errors.EnvironmentError;
import samuelpalmer.sensorautorotation.utilities.ObjectExtensions;

public class ApplicationStatus {
	
	/**
	 * If the application is not running, this is {@code null}.
	 * Otherwise this is the state of the running instance of the application.
	 */
	@Nullable
	public final RunningState runningState;
	
	/**
	 * The current error or [@code null} if there is none.
	 */
	@Nullable
	public final EnvironmentError error;
	
	public ApplicationStatus(@Nullable RunningState runningState, @Nullable EnvironmentError error) {
		this.runningState = runningState;
		this.error = error;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()))
			return false;
		
		ApplicationStatus other = (ApplicationStatus)obj;
		
		return
			ObjectExtensions.areEqual(this.runningState, other.runningState)
			&&
			ObjectExtensions.areEqual(this.error, other.error);
	}
	
	private static final int MSG_WHAT = 1;
	
	public ApplicationStatus(Message message) {
		if (message.arg1 == 0)
			this.runningState = null;
		else if (message.arg1 == 1)
			this.runningState = new RunningState(message.arg2);
		else
			throw new IllegalArgumentException("Found unknown value in arg1: " + message.arg1);
		
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
			runningState == null ? 0 : 1,
			runningState == null ? 0 : runningState.serialise()
		);
		
		if (error != null) {
			Bundle bundle = new Bundle();
			bundle.putString(null, error.toJson());
			result.setData(bundle);
		}
		
		return result;
	}
	
	public static class RunningState {
		
		public final UserSide userSide;
		public final boolean waitingForSensor;
		
		public RunningState(UserSide userSide, boolean waitingForSensor) {
			this.userSide = userSide;
			this.waitingForSensor = waitingForSensor;
		}
		
		RunningState(int serialised) {
			this.userSide = (serialised & 0x1) == 0 ? UserSide.LEFT : UserSide.RIGHT;
			this.waitingForSensor = (serialised & 0x2) != 0;
			
			if (serialised >> 2 != 0)
				throw new IllegalArgumentException("Unknown serialised value: " + serialised);
		}
		
		int serialise() {
			return
				(userSide.equals(UserSide.LEFT) ? 0 : 1)
				|
				(waitingForSensor ? 1 : 0) << 1;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !obj.getClass().equals(this.getClass()))
				return false;
			
			RunningState other = (RunningState)obj;
			
			return
				this.userSide.equals(other.userSide)
				&&
				this.waitingForSensor == other.waitingForSensor;
		}
		
	}
	
}
