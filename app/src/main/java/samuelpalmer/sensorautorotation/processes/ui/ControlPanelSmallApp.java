package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;

import com.sony.smallapp.SmallApplication;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ControlPanelSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		Intent intent = new Intent(this, ControlPanelActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

}
