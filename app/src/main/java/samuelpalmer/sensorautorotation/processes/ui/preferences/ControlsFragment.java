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
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsFragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsSummary;


public class ControlsFragment extends PreferenceFragment {

	private static final int REQUEST_CODE_CHANGE_NOTIFICATION_SETTINGS = 1;

	private NotificationsSummary notificationsSummary;
	private Preference notificationsPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_controls);

		notificationsPreference = findPreference("notifications");
		notificationsPreference.setOnPreferenceClickListener(notificationsClicked);
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
		
		Preference assistPreference = findPreference("assist");
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
		if (Build.VERSION.SDK_INT < 16)
			getPreferenceScreen().removePreference(assistPreference);
		
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
		updateNotificationsSummary();
	}

	@Override
	public void onStop() {
		super.onStop();

		notificationsSummary.unsubscribe(notificationsChanged);
	}

	private final Preference.OnPreferenceClickListener notificationsClicked = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
			//Notification configuration is built into Android O, so we'll just send the user to the relevant OS settings screen.
			if (Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1) {
				Intent intent;

				if (SystemServices.notificationManager().areNotificationsEnabled())
					//Guide the user to block just the dashboard notification
					intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
							.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannels.ID_CONTROLS);
				else
					//Guide the user to unblock the app's notifications and toasts
					intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

				intent.putExtra(Settings.EXTRA_APP_PACKAGE, getActivity().getPackageName());

				startActivityForResult(intent, REQUEST_CODE_CHANGE_NOTIFICATION_SETTINGS);
			} else
				startActivity(new Intent(getActivity(), NotificationsActivity.class));

			return true;
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_CHANGE_NOTIFICATION_SETTINGS)
			NotificationsFragment.updateServiceNotification();
	}

	private final EventHandler notificationsChanged = new EventHandler() {
		@Override
		public void update() {
			updateNotificationsSummary();
		}
	};

	private void updateNotificationsSummary() {
		CharSequence summary = notificationsSummary.getSummary();
		notificationsPreference.setSummary(summary);
	}

}
