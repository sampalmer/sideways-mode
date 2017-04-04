package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;

import samuelpalmer.sensorautorotation.R;

class AutoRotationMode implements Mode {

	@Override
	public int notificationIcon() {
		return R.drawable.notification_rotation;
	}

	@Override
	public int actionIcon() {
		return R.drawable.action_rotation;
	}

	@Override
	public int dashboardIcon() {
		return R.drawable.dashboard_rotation;
	}

	@Override
	public int autoRotationStateResourceId() {
		return R.string.screen_rotation_enabled;
	}

	@Override
	public int toggleToOtherModeResourceId() {
		return R.string.action_disable_auto_rotation;
	}

	@Override
	public boolean equals(Mode other) {
		return getClass() == other.getClass();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {}

	public static final Creator<AutoRotationMode> CREATOR = new Creator<AutoRotationMode>() {
		@Override
		public AutoRotationMode createFromParcel(Parcel in) {
			return new AutoRotationMode();
		}

		@Override
		public AutoRotationMode[] newArray(int size) {
			return new AutoRotationMode[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
