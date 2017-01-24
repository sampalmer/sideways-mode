package samuelpalmer.sensorautorotation.processes.service.controlpanel;

import android.app.Dialog;
import android.util.Log;

public class ControlPanelCoordinator {

	private static Dialog dialog;

	public static void created(Dialog dialog) {
		if (ControlPanelCoordinator.dialog != null)
			throw new RuntimeException("Already have a dialog");

		ControlPanelCoordinator.dialog = dialog;
		Log.i(ControlPanelDialog.class.getSimpleName(), "Created dialog");
	}

	public static void destroyed() {
		ControlPanelCoordinator.dialog = null;
		Log.i(ControlPanelCoordinator.class.getSimpleName(), "Finished up dialog");
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
