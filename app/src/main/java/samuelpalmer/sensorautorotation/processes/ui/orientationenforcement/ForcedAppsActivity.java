package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.app.Activity;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;

//TODO: Warn the user that apps aren't guaranteed to work correctly when using this mode
public class ForcedAppsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.forced_apps_activity);
	}

}
