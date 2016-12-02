package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class PopupControlPanelActivity extends UiActivity {

	private Settings.Setting<Boolean> enabledSetting;

	//TODO: Maybe also close this when the screen turns off?
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//TODO: Instead of doing all this, just make the view enter a visually-disabled state
		enabledSetting = ApplicationSettings.remote().enabled();
		if (!enabledSetting.get()) {
			RotationServiceClient.showDisabledMessage();
			finish();
		}

		enabledSetting.subscribe(subscriber);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
//		window.setWindowAnimations(android.R.anim.);

		setContentView(R.layout.control_panel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		enabledSetting.unsubscribe(subscriber);
		enabledSetting = null;
	}

	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			if (!enabledSetting.get())
				PopupControlPanelActivity.this.finish();
		}
	};

	@Override
	public void setFinishOnTouchOutside(boolean finish) {
		super.setFinishOnTouchOutside(finish);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//Not stopping the activity here since opening this on the lock screen seems to cause the activity to stop and start again before becoming visible to the user.
	}
}
