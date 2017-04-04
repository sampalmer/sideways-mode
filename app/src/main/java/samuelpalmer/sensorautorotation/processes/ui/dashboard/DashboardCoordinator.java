package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.util.Log;

/**
 * Keeps track of active dashboard dialogs. Useful for ensuring only one is visible at a time.
 */
public class DashboardCoordinator {

	private static Dialog dialog;

	public static void created(Dialog dialog) {
		if (DashboardCoordinator.dialog != null)
			throw new RuntimeException("Already have a dialog");

		DashboardCoordinator.dialog = dialog;
		Log.i(DashboardDialog.class.getSimpleName(), "Created dialog");
	}

	public static void destroyed() {
		DashboardCoordinator.dialog = null;
		Log.i(DashboardCoordinator.class.getSimpleName(), "Finished up dialog");
	}

	public static boolean exists() {
		return dialog != null;
	}

	/**
	 * This is useful in case the dialog is present but hidden
	 */
	public static void show() {
		ensureExists();

		dialog.show();
	}

	public static void dismiss() {
		ensureExists();

		dialog.dismiss();
	}

	private static void ensureExists() {
		if (dialog == null)
			throw new RuntimeException("Don't have a dialog");
	}
}
