package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.view.View;

public abstract class ViewHelpers {
	
	/**
	 * Only changes the visibility if it's different to the current value.
	 * This is to minimise lag.
	 */
	public static void ensureVisibility(View control, int newVisibility) {
		int currentVisibility = control.getVisibility();
		if (currentVisibility != newVisibility)
			control.setVisibility(newVisibility);
	}
	
}
