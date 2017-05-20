package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

/**
 * Keeps track of active dashboard dialogs. Useful for ensuring only one is visible at a time.
 */
public class DashboardDialogCoordinator {

	private static Dialog dialog;

	public static Dialog createDialog(Context context, final Runnable onStopped) {
		DashboardDialog.Listener onStart = new DashboardDialog.Listener() {
			@Override
			public void onChange(Dialog dialog) {
				if (DashboardDialogCoordinator.dialog != null)
					throw new RuntimeException("Already have a dialog");

				DashboardDialogCoordinator.dialog = dialog;
				Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Dialog started");
			}
		};

		DashboardDialog.Listener onStop = new DashboardDialog.Listener() {
			@Override
			public void onChange(Dialog dialog) {
				if (DashboardDialogCoordinator.dialog == null)
					throw new RuntimeException("Don't have a dialog");
				if (DashboardDialogCoordinator.dialog != dialog)
					throw new RuntimeException("Unrecognised dialog");

				DashboardDialogCoordinator.dialog = null;
				Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Dialog stopped");
				onStopped.run();
			}
		};

		Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Created dialog");

		return new DashboardDialog(context, onStart, onStop);
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
