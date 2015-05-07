package samuelpalmer.sensorautorotation.orientationenforcement.ui;

import java.util.Comparator;

public class InstalledAppComparator implements Comparator<AppPackagesPreference.InstalledApp> {
	@Override
	public int compare(AppPackagesPreference.InstalledApp lhs, AppPackagesPreference.InstalledApp rhs) {
		return lhs.label.toString().compareToIgnoreCase(rhs.label.toString());
	}
}
