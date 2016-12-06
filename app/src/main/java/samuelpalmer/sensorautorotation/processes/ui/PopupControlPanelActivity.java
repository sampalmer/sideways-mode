package samuelpalmer.sensorautorotation.processes.ui;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.R;

public class PopupControlPanelActivity extends UiActivity {

	private static final int windowAnimationStyle = android.R.style.Animation_Dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		window.setWindowAnimations(windowAnimationStyle);

		setContentView(R.layout.control_panel);
	}

	@Override
	public void finish() {
		super.finish();

		// The default exit animation seems to be the activity exit animation, which,
		// on Android 7.1 at least, is the slide-down animation.
		// I don't think this looks good for a dialog, so I'm trying to force a dialog exit animation instead.
		int[] attrs = { android.R.attr.windowExitAnimation };
		TypedArray typedArray = obtainStyledAttributes(windowAnimationStyle, attrs);
		int exitAnimation = typedArray.getResourceId(0, 0);
		typedArray.recycle();

		overridePendingTransition(0,  exitAnimation);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Not stopping the activity here since opening this on the lock screen seems to cause the activity to stop and start again before becoming visible to the user.
	}

}
