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

		if (!new Settings(this).enabled().getOr(false))
			Toast
				.makeText(this, R.string.not_enabled_message, Toast.LENGTH_SHORT)
				.show();
		else
			new ServiceController(this).ensureStarted(RotationService.getToggleAction(this));

		finish();
	}

}
