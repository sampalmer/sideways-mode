package samuelpalmer.sensorautorotation.settings.application.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class ParcelableEditor implements Parcelable {

	public final Map<String, Object> mModified = new HashMap<>();;
	public boolean mClear;

	public ParcelableEditor() {}

	private ParcelableEditor(Parcel source) {
		source.readMap(mModified, null);
		mClear = (boolean) source.readValue(null);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeMap(mModified);
		dest.writeValue(mClear);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<ParcelableEditor> CREATOR
			= new Creator<ParcelableEditor>() {
		@Override
		public ParcelableEditor createFromParcel(Parcel source) {
			return new ParcelableEditor(source);
		}

		@Override
		public ParcelableEditor[] newArray(int size) {
			return new ParcelableEditor[size];
		}
	};
}
