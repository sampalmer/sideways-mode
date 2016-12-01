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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		enabledSetting = ApplicationSettings.remote().enabled();
		if (!enabledSetting.get()) {
			RotationServiceClient.showDisabledMessage();
			finish();
		}

		enabledSetting.subscribe(subscriber);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

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
	protected void onPause() {
		super.onPause();
		finish(); //TODO: This actually prevents this from working when launched from the lock screen
	}
}
