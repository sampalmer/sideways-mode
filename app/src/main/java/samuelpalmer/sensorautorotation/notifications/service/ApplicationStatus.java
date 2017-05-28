package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

@SuppressWarnings("WeakerAccess")
public class ApplicationStatus implements Parcelable {
	
	public final Mode mode;
	public final boolean overridden;
	public final UserOrientation user;
	public final NotificationPriority priority;
	public final boolean waitingForSensor;

	public ApplicationStatus(
		Mode mode,
		boolean overridden,
		UserOrientation user,
		NotificationPriority priority,
	    boolean waitingForSensor
	) {
		this.mode = mode;
		this.overridden = overridden;
		this.user = user;
		this.priority = priority;
		this.waitingForSensor = waitingForSensor;
	}

	private ApplicationStatus(Parcel in) {
		mode = in.readParcelable(this.getClass().getClassLoader());
		overridden = in.readByte() != 0;
		user = in.readParcelable(this.getClass().getClassLoader());
		priority = NotificationPriority.valueOf(in.readString());
		waitingForSensor = in.readByte() != 0;
		
		if (in.dataAvail() != 0)
			throw new RuntimeException("Found more data in parcel than expected");
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mode, 0);
		dest.writeByte((byte) (overridden ? 1 : 0));
		dest.writeParcelable(user, 0);
		dest.writeString(priority.name());
		dest.writeByte((byte) (waitingForSensor ? 1 : 0));
	}

	public CharSequence getSummary() {
		CharSequence status =
			waitingForSensor
			?
			StringResource.get(R.string.status_waiting_sensor)
			:
			StringResource.format(R.string.status_user_phrase, user.descriptionId());
		
		return StringResource.format(R.string.notification_summary, mode.autoRotationStateResourceId(), status);
	}
	
	@DrawableRes
	public int getIcon() {
		return overridden ? user.iconId() : mode.notificationIcon();
	}
	
	public boolean equals(ApplicationStatus other) {
		return
			mode.equals(other.mode)
			&&
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
