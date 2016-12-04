package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.R;

public class PopupControlPanelActivity extends UiActivity {

	//TODO: Maybe also close this when the screen turns off?
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//		window.setWindowAnimations(android.R.anim.);

		setContentView(R.layout.control_panel);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Not stopping the activity here since opening this on the lock screen seems to cause the activity to stop and start again before becoming visible to the user.
	}
}
