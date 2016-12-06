package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import java.util.Comparator;

class InstalledAppComparator implements Comparator<InstalledApp> {
	@Override
	public int compare(InstalledApp lhs, InstalledApp rhs) {
		return lhs.label.toString().compareToIgnoreCase(rhs.label.toString());
	}
}
