package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.ServiceNotificationHider;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class NotificationsFragment extends PreferenceFragment {

	private final EventHandler listPreferenceSummaryUpdater = new EventHandler() {
		@Override
		public void update() {
			updateListPreferenceSummaries(getPreferenceScreen());
		}
	};

	private Preference allNotificationsPreference;
	private NotificationManager notificationManager;

	//TODO: See if you can move this logic into an AutoSummaryListPreference
	private void updateListPreferenceSummaries(PreferenceGroup group) {
		for (int i = 0; i < group.getPreferenceCount(); ++i) {
			Preference preference = group.getPreference(i);
			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntry());
			}
			else if (preference instanceof PreferenceGroup) {
				updateListPreferenceSummaries((PreferenceGroup) preference);
			}
		}
	}

	private EventRegistrar notificationPriorityChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		notificationManager = SystemServices.notificationManager();

		addPreferencesFromResource(R.xml.preferences_notifications);
		allNotificationsPreference = findPreference("allNotifications");
		allNotificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				new AlertDialog.Builder(getActivity())
						.setTitle(R.string.preference_all_notifications_title)
						.setMessage(R.string.preference_all_notifications_explanation)
						.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								openAppNotificationSystemSettings(); //TODO: Once the user returns, regenerate the notification in case they've just enabled it.
							}
						})
						.show();

				return true;
			}
		});

		updateListPreferenceSummaries(getPreferenceScreen());
		hideUnsupportedOptions();

		notificationPriorityChanged = ApplicationSettings.remote().notificationPriority();
		notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);
	}

	private void openAppNotificationSystemSettings() {
		Intent appNotificationSettingsIntent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS")
			.putExtra("app_package", CurrentProcess.context().getPackageName())
			.putExtra("app_uid", CurrentProcess.context().getApplicationInfo().uid);

		try {
			startActivity(appNotificationSettingsIntent);
		} catch (Exception ex) {
			Log.w(NotificationsFragment.class.getSimpleName(), "Couldn't directly open app notification settings. Opening app settings instead.", ex);

			Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					.setData(Uri.fromParts("package", CurrentProcess.context().getPackageName(), null));

			startActivity(appSettingsIntent);
			//TODO: This activity might not exist either. Decide what to do.
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
	}

	@TargetApi(Build.VERSION_CODES.N)
	@Override
	public void onStart() {
		super.onStart();

		if (!ServiceNotificationHider.isSupported()) {
			boolean notificationsAllowed = notificationManager.areNotificationsEnabled();
			allNotificationsPreference.setSummary(notificationsAllowed ? R.string.preference_all_notifications_summary_allowed : R.string.preference_all_notifications_summary_blocked);
		}
	}

	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(ApplicationSettings.remote().notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
		}

		if (!ServiceNotificationHider.isSupported()) {
			Preference showNotificationPreference = findPreference(ApplicationSettings.remote().showNotification().getKey());
			getPreferenceScreen().removePreference(showNotificationPreference);
		}
		else
			getPreferenceScreen().removePreference(allNotificationsPreference);
	}

}
