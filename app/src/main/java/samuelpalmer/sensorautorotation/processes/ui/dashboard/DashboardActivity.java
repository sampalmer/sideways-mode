package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class DashboardActivity extends DialogActivity {

	private static final String TAG = "dialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (DashboardCoordinator.exists()) {
			finish();
			DashboardCoordinator.show();
			return;
		}

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		if (getFragmentManager().findFragmentByTag(TAG) == null) {
			DashboardDialog dialog = new DashboardDialog.Builder(this).create();
			dialog.show(getFragmentManager(), TAG);
		}
	}

}
