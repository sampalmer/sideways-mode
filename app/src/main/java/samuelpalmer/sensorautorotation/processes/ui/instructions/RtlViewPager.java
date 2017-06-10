package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.AbsSavedState;
import android.util.AttributeSet;

/**
 * Fixes a bug in the underlying implementation where changing the system
 * language between LTR and RTL results in the current item being mixed up.
 *
 * The underlying bug should be fixed when in the next release of RtlViewPager (> 1.0.1)
 */
public class RtlViewPager extends com.booking.rtlviewpager.RtlViewPager {
	
	public RtlViewPager(Context context) {
		super(context);
	}
	
	public RtlViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, getCurrentItem());
	}
	
	@Override
	public void onRestoreInstanceState(Parcelable uncastState) {
		SavedState state = (SavedState) uncastState;
		super.onRestoreInstanceState(state.getSuperState());
		setCurrentItem(state.currentItem, false);
	}
	
	private static class SavedState extends AbsSavedState {
		
		private final int currentItem;
		
		protected SavedState(Parcelable superState, int currentItem) {
			super(superState);
			this.currentItem = currentItem;
		}
		
		protected SavedState(Parcel source) {
			super(source, SavedState.class.getClassLoader());
			currentItem = source.readInt();
		}
		
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}
			
			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
			
		};
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentItem);
		}
		
	}
	
}
