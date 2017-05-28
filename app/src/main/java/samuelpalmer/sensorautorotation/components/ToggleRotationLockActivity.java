package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceClient;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;

/**
 * This MUST be in the "samuelpalmer.sensorautorotation.components" package for backwards
 * compatibility since existing shortcuts on users' devices seem to reference this by
 * fully-qualified class name.
 */
public class ToggleRotationLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ApplicationInstanceCommand command = new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_TOGGLE);
		command.showToast = true;

		ApplicationInstanceClient.send(command);
		finish();
	}

}
