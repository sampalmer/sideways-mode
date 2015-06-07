package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.processes.service.RotationLockToggler;

public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RotationLockToggler.toggle();
		finish();
	}

}
