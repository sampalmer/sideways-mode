package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class DashboardActivity extends DialogActivity {
	
	private AlertDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dialog = new AlertDialog.Builder(this).create();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				dialog = null;
				DashboardActivity.this.finish();
			}
		});
		dialog.setView(new DashboardView(this, null));
		dialog.show();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
