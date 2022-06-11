package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import java.text.Collator;
import java.util.Comparator;

/**
 * Sorts {@link InstalledApp} by app name
 */
class InstalledAppComparator implements Comparator<InstalledApp> {
    
    private final Collator collator;
    
    public InstalledAppComparator() {
        collator = Collator.getInstance();
    }
    
    @Override
    public int compare(InstalledApp lhs, InstalledApp rhs) {
        int result = collator.compare(lhs.label.toString(), rhs.label.toString());
        if (result != 0)
            return result;
        
        return lhs.packageName.compareTo(rhs.packageName);
    }
    
}
