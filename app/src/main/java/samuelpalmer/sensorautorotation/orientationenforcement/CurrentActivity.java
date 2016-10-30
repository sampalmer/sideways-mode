package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

public class CurrentActivity {

	private final ActivityInfo activity;
	private final ComponentName componentName;

	public CurrentActivity(ActivityInfo activity) {
		this.activity = activity;
		componentName = new ComponentName(activity.packageName, activity.name);
	}

	public int declaredScreenOrientation() {
		return activity.screenOrientation;
	}

	@Override
	public boolean equals(Object o) {
		return
				o != null
				&&
				o.getClass().equals(CurrentActivity.class)
				&&
				componentName.equals(((CurrentActivity) o).componentName);
	}

	@Override
	public String toString() {
		return componentName.toShortString();
	}

	public String getPackage() {
		return activity.packageName;
	}
}
