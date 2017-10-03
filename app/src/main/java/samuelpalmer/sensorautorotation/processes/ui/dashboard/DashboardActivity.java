package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

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
		
		dialog = DashboardDialogCoordinator.createDialog(this, new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				dialog = null;
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
	
}
