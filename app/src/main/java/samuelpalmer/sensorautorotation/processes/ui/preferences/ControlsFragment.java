package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsSummary;


public class ControlsFragment extends PreferenceFragment {

	private NotificationsSummary notificationsSummary;
	private Preference notificationsPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_controls);

		notificationsPreference = findPreference("notifications");
		notificationsSummary = new NotificationsSummary();
	}

	@Override
	public void onStart() {
		super.onStart();

		notificationsSummary.subscribe(notificationsChanged);
		updateNotifications();
	}

	@Override
	public void onStop() {
		super.onStop();

		notificationsSummary.unsubscribe(notificationsChanged);
	}

	private EventHandler notificationsChanged = new EventHandler() {
		@Override
		public void update() {
			updateNotifications();
		}
	};

	private void updateNotifications() {
		CharSequence summary = notificationsSummary.getSummary();
		notificationsPreference.setSummary(summary);
	}

}
