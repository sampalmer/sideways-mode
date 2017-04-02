package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.os.Bundle;
import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;


public class ForcedAppsActivity extends PreferencesActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forced_apps_activity);
	}

}
