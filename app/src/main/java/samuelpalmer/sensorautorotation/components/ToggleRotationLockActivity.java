package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.RotationLockToggler;

public class ToggleRotationLockActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new RotationLockToggler(this).go(true);
		finish();
	}
}
