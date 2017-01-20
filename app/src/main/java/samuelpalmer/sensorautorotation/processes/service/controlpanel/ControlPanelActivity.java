package samuelpalmer.sensorautorotation.processes.service.controlpanel;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class ControlPanelActivity extends DialogActivity {

	private static final String TAG = "dialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ControlPanelCoordinator.exists()) {
			finish();
			ControlPanelCoordinator.show();
			return;
		}

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		if (getFragmentManager().findFragmentByTag(TAG) == null) {
			ControlPanelDialog dialog = new ControlPanelDialog.Builder(this).create();
			dialog.show(getFragmentManager(), TAG);
		}
	}

}
