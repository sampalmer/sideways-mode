package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.R;

public class PopupControlPanelActivity extends Activity {

	private static final String TAG = "dialog";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		setContentView(R.layout.control_panel);

//		if (getFragmentManager().findFragmentByTag(TAG) == null)
//			new PopupControlPanelDialog().show(getFragmentManager(), TAG);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}
