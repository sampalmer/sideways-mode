package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.os.Bundle;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.RemotePreferencesActivity;

public class NotificationsActivity extends RemotePreferencesActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
	}

}
