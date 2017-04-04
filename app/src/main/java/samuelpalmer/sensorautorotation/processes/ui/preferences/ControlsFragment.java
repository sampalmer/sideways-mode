package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.annotation.TargetApi;
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

		Preference quickSettingPreference = findPreference("quick_setting");
		quickSettingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.preference_quick_setting)
						.setMessage(R.string.preference_quick_setting_instructions)
						.show();

				return true;
			}
		});
		if (Build.VERSION.SDK_INT < 24)
			getPreferenceScreen().removePreference(quickSettingPreference);

		assistPreference = findPreference("assist");
		assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Build.VERSION.SDK_INT >= 21)
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.preference_assist)
							.setMessage(R.string.preference_assist_instructions_lollipop)
							.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							})
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@TargetApi(Build.VERSION_CODES.LOLLIPOP)
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//TODO: Ensure this doesn't crash; the activity might not exist
									startActivity(new Intent(Settings.ACTION_VOICE_INPUT_SETTINGS));
								}
							})
							.show();
				else
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.preference_assist)
							.setMessage(R.string.preference_assist_instructions)
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							})
							.show();

				return true;
			}
		});

		Preference smallAppPreference = findPreference("small_app");
		smallAppPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.preference_small_app)
						.setMessage(R.string.preference_small_app_instructions)
						.show();

				return true;
			}
		});
		if (!supportsSmallApp())
			getPreferenceScreen().removePreference(smallAppPreference);
	}

	private static boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}

		return true;
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

	private final EventHandler notificationsChanged = new EventHandler() {
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
