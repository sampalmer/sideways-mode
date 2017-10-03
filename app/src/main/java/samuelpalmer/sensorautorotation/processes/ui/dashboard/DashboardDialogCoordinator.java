package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Keeps track of active dashboard dialogs. Useful for ensuring only one is visible at a time.
 */
public class DashboardDialogCoordinator {

	private static AlertDialog dialog;

	public static Dialog createDialog(Context context, final Dialog.OnDismissListener listener) {
		dialog = new AlertDialog.Builder(context).create();
		dialog.setView(new DashboardView(context, null));
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				dialog = null;
				Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Dialog dismissed");
				
				listener.onDismiss(dialogInterface);
			}
		});
		
		Log.i(DashboardDialogCoordinator.class.getSimpleName(), "Dialog created");
		
		return dialog;
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
