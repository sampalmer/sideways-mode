package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;
import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.R;


public class SettingsActivity extends PreferencesActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);
	}

}
