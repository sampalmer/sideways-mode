package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class DashboardActivity extends DialogActivity {

	private Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DashboardDialogCoordinator.dialogExists()) {
			Log.i(DialogActivity.class.getSimpleName(), "Dialog already exists. Aborting.");
			finish();
			DashboardDialogCoordinator.showDialog();
			return;
		}

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		dialog = DashboardDialogCoordinator.createDialog(this, new Runnable() {
			@Override
			public void run() {
				finish();
			}
		});

		dialog.show();
	}

	//TODO: See if this is needed now that DashboardDialog does its own deinit in onStop
	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(DialogActivity.class.getSimpleName(), "Destroying");

		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

}
