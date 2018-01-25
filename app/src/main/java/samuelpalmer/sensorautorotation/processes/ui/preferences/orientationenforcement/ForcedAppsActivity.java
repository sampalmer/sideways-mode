package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.os.Bundle;
import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;


public class ForcedAppsActivity extends UiActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forced_apps);
	}

}
