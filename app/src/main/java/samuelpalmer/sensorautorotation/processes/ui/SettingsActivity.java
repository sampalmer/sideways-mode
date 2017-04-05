package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;


public class SettingsActivity extends PreferencesActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);
	}

}
