package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsSummary;


public class ControlsFragment extends PreferenceFragment {

	private NotificationsSummary notificationsSummary;
	private Preference notificationsPreference;
	private Preference assistPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_controls);

		notificationsPreference = findPreference("notifications");
		notificationsSummary = new NotificationsSummary();

		assistPreference = findPreference("assist");
		assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				//TODO: Also make sure to explain how to use the app once it's configured

				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.preference_assist)
						.setMessage(R.string.preference_all_notifications_explanation) //TODO: Set an appropriate description
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								instructUserToEnableAssist();
							}
						})
						.show();

				return true;
			}
		});
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

	private void instructUserToEnableAssist() {
		if (Build.VERSION.SDK_INT >= 21)
			//TODO: Check if this works on the Samsung phone as well
			startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
		else {
			//TODO: Instruct user to just swipe up from the home button to configure it
		}
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
