package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.R;

public class PopupControlPanelActivity extends UiActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		setContentView(R.layout.control_panel);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		//This makes the popup go away when you pull down the notification drawer
		if (!hasFocus)
			finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}
