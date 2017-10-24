package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;

import com.sony.smallapp.SmallApplication;

import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Shortcut to open {@link DashboardActivity} from the small app tray.
 */
public class DashboardSmallApp extends SmallApplication {

	@Override
	protected void onCreate() {
		super.onCreate();
		Intent intent = new Intent(this, DashboardActivity.class).addFlags(FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

}
