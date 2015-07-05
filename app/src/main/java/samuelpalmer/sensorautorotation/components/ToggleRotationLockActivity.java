package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.processes.service.RotationLockToggler;
import samuelpalmer.sensorautorotation.processes.service.ServiceProcess;

/**
 * This MUST be in the "samuelpalmer.sensorautorotation.components" package for backwards
 * compatibility since existing shortcuts on users' devices seem to reference this by
 * fully-qualified class name.
 */
public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ServiceProcess.ensureInitialised();
		RotationLockToggler.toggle();
		finish();
	}

}
