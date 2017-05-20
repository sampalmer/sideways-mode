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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		Log.i(DialogActivity.class.getSimpleName(), "Destroying");
		
		//Prevents a window leak error
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		/*
		 * Since this activity can be shown on the lock screen,
		 * leaving the activity open when you lock your phone would
		 * result in it being shown when you next unlock your phone.
		 * (At least in Android 7.1.1.)
		 *
		 * Finishing the activity on stop seems to fix this nicely.
		 * Note that this only works because the activity is declared as
		 * supporting screen size, orientation & layout config changes. If it wasn't,
		 * changes to those properties would also result in the activity finishing.
		 */
		finish();
	}
	
}
