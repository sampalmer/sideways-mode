package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceClient;

/**
 * This MUST be in the "samuelpalmer.sensorautorotation.components" package for backwards
 * compatibility since existing shortcuts on users' devices seem to reference this by
 * fully-qualified class name.
 */
public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RotationServiceClient.send(RotationService.ACTION_TOGGLE);
		finish();
	}

}
