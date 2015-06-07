package samuelpalmer.sensorautorotation.processes.service;

import android.app.Activity;
import android.os.Bundle;

public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RotationLockToggler.toggle();
		finish();
	}

}
