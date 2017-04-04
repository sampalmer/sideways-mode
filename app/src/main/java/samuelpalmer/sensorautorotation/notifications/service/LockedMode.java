package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;

class LockedMode implements Mode {

	private final boolean portrait;

	public LockedMode(OrientationCalculator calculator, boolean deviceIsNaturallyPortrait) {
		boolean inNaturalOrientation = calculator.proposedScreenOrientation().isNaturalOrUpsideDown();
		portrait = inNaturalOrientation == deviceIsNaturallyPortrait;
	}

	public LockedMode(Parcel in) {
		portrait = in.readByte() != 0;
	}

	@Override
	public int notificationIcon() {
		return portrait ? R.drawable.portrait_lock : R.drawable.landscape_lock;
	}

	@Override
	public int actionIcon() {
		return portrait ? R.drawable.action_portrait_lock : R.drawable.action_landscape_lock;
	}

	@Override
	public int dashboardIcon() {
		return portrait ? R.drawable.dashboard_portrait_lock : R.drawable.dashboard_landscape_lock;
	}

	@Override
	public int autoRotationStateResourceId() {
		return portrait ? R.string.screen_rotation_disabled_portrait : R.string.screen_rotation_disabled_landscape;
	}

	@Override
	public int toggleToOtherModeResourceId() {
		return R.string.action_enable_auto_rotation;
	}

	@Override
	public boolean equals(Mode other) {
		return
			LockedMode.class.equals(other.getClass())
			&&
			portrait == ((LockedMode)other).portrait;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (portrait ? 1 : 0));
	}

	public static final Creator<LockedMode> CREATOR = new Creator<LockedMode>() {
		@Override
		public LockedMode createFromParcel(Parcel in) {
			return new LockedMode(in);
		}

		@Override
		public LockedMode[] newArray(int size) {
			return new LockedMode[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
