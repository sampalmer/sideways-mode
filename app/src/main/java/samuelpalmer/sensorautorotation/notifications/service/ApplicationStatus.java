package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus implements Parcelable {
	
	public final UserOrientation user;
	public final boolean waitingForSensor;

	public ApplicationStatus(
		UserOrientation user,
	    boolean waitingForSensor
	) {
		this.user = user;
		this.waitingForSensor = waitingForSensor;
	}

	private ApplicationStatus(Parcel in) {
		user = in.readParcelable(this.getClass().getClassLoader());
		waitingForSensor = in.readByte() != 0;
		
		if (in.dataAvail() != 0)
			throw new RuntimeException("Found more data in parcel than expected");
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, 0);
		dest.writeByte((byte) (waitingForSensor ? 1 : 0));
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			user.equals(other.user)
			&&
			waitingForSensor == other.waitingForSensor;
	}
	
	public static final Creator<ApplicationStatus> CREATOR = new Creator<ApplicationStatus>() {
		@Override
		public ApplicationStatus createFromParcel(Parcel in) {
			return new ApplicationStatus(in);
		}

		@Override
		public ApplicationStatus[] newArray(int size) {
			return new ApplicationStatus[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
}
