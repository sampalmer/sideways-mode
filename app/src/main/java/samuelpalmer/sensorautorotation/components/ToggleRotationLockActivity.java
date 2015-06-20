package samuelpalmer.sensorautorotation.components;

import samuelpalmer.sensorautorotation.processes.service.RotationLockToggler;
import samuelpalmer.sensorautorotation.processes.service.ServiceActivity;

/**
 * This MUST be in the "samuelpalmer.sensorautorotation.components" package for backwards
 * compatibility since existing shortcuts on users' devices seem to reference this by
 * fully-qualified class name.
 */
public class ToggleRotationLockActivity extends ServiceActivity {

	@Override
	protected void onCreate() {
		RotationLockToggler.toggle();
		finish();
	}

}
