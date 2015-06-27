package samuelpalmer.sensorautorotation.processes.ui.instructions;

import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.ServiceSyncActivity;

public class InstructionsActivity extends ServiceSyncActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions_activity);
	}

}
