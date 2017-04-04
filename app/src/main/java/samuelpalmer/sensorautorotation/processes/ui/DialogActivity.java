package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Build;

public abstract class DialogActivity extends UiActivity {

	@Override
	protected void onStart() {
		super.onStart();

		//Prevents the error: "Activity {samuelpalmer.sensorautorotation/samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity} did not call finish() prior to onResume() completing"
		if (Build.VERSION.SDK_INT >= 23)
			setVisible(true);
	}

}
