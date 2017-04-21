package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.ui.preferences.PreferencesActivity;

public class NotificationsActivity extends PreferencesActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Notification configuration is built into Android O, so we'll just send the user to the relevant OS settings screen.
		if (Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1) {
			Intent intent;

			if (SystemServices.notificationManager().areNotificationsEnabled())
				//Guide the user to block just the dashboard notification
				intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
						.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.ID_DASHBOARD);
			else
				//Guide the user to unblock the app's notifications and toasts
				intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

			intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());

			startActivity(intent);
			finish();
			return;
		}

		setContentView(R.layout.activity_notifications);
	}

}
