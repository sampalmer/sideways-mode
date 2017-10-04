package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;

import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class DashboardActivity extends DialogActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new DashboardView(this, null));
	}
	
}
