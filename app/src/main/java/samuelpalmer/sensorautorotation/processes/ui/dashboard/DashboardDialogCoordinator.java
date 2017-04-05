package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.util.Log;

/**
 * Keeps track of active dashboard dialogs. Useful for ensuring only one is visible at a time.
 */
public class DashboardDialogCoordinator {

	private static Dialog dialog;

	public static void created(Dialog dialog) {
		if (DashboardDialogCoordinator.dialog != null)
			throw new RuntimeException("Already have a dialog");

		DashboardDialogCoordinator.dialog = dialog;
		Log.i(DashboardDialog.class.getSimpleName(), "Created dialog");
	}

	public static void destroyed() {
		DashboardDialogCoordinator.dialog = null;
		Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Finished up dialog");
	}

	public static boolean dialogExists() {
		return dialog != null;
	}

	/**
	 * This is useful in case the dialog is present but hidden
	 */
	public static void showDialog() {
		ensureExists();

		dialog.show();
	}

	public static void dismissDialog() {
		ensureExists();

		dialog.dismiss();
	}

	private static void ensureExists() {
		if (dialog == null)
			throw new RuntimeException("Don't have a dialog");
	}
}
