package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus implements Parcelable {
	
	public final boolean overridden;
	public final UserOrientation user;
	public final NotificationPriority priority;
	public final boolean waitingForSensor;

	public ApplicationStatus(
		boolean overridden,
		UserOrientation user,
		NotificationPriority priority,
	    boolean waitingForSensor
	) {
		this.overridden = overridden;
		this.user = user;
		this.priority = priority;
		this.waitingForSensor = waitingForSensor;
	}

	private ApplicationStatus(Parcel in) {
		overridden = in.readByte() != 0;
		user = in.readParcelable(this.getClass().getClassLoader());
		priority = NotificationPriority.valueOf(in.readString());
		waitingForSensor = in.readByte() != 0;
		
		if (in.dataAvail() != 0)
			throw new RuntimeException("Found more data in parcel than expected");
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (overridden ? 1 : 0));
		dest.writeParcelable(user, 0);
		dest.writeString(priority.name());
		dest.writeByte((byte) (waitingForSensor ? 1 : 0));
	}
	
	@DrawableRes
	public int getIcon() {
		return overridden ? user.iconId() : R.drawable.notification_rotation;
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			overridden == other.overridden
			&&
			user.equals(other.user)
			&&
			priority.equals(other.priority)
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
