package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import java.text.Collator;
import java.util.Comparator;

class InstalledAppComparator implements Comparator<InstalledApp> {
	
	private final Collator collator;
	
	public InstalledAppComparator() {
		collator = Collator.getInstance();
	}
	
	@Override
	public int compare(InstalledApp lhs, InstalledApp rhs) {
		return collator.compare(lhs.label.toString(), rhs.label.toString());
	}
	
}
