package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.os.Bundle;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;


public class IncompatibleAppsActivity extends UiActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_incompatible_apps);
		setTitle(R.string.preference_incompatible_apps_title);
	}

}
