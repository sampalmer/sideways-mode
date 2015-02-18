package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.RotationLockToggler;

public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new RotationLockToggler(this, false, callback).go();
	}

	private final RotationLockToggler.CompletionCallback callback = new RotationLockToggler.CompletionCallback() {
		@Override
		public void completed() {
			finish();
		}
	};

}
