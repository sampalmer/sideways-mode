package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;

public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (enabled())
			toggle();
		else
			showNotEnabledMessage();

		finish();
	}

	private Boolean enabled() {
		return new Settings(this).enabled().getOr(false);
	}

	private void toggle() {
		new ServiceController(this).ensureStarted(RotationService.getToggleAction(this));
	}

	private void showNotEnabledMessage() {
		Toast
			.makeText(this, R.string.not_enabled_message, Toast.LENGTH_SHORT)
			.show();
	}

}
