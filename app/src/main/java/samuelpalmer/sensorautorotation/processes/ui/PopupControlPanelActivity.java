package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class PopupControlPanelActivity extends DialogActivity {

	private static final String TAG = "dialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		if (getFragmentManager().findFragmentByTag(TAG) == null)
			new ControlPanelDialog.Builder(this).create().show(getFragmentManager(), TAG);
	}

}
