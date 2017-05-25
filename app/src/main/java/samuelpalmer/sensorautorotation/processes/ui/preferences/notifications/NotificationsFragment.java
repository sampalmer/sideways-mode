package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceCommand;
import samuelpalmer.sensorautorotation.processes.service.RotationServiceReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class NotificationsFragment extends PreferenceFragment {

	private static final int REQUEST_CODE_ALL_NOTIFICATIONS_CHANGED = 1;

	private final EventHandler listPreferenceSummaryUpdater = new EventHandler() {
		@Override
		public void update() {
			updateListPreferenceSummaries(getPreferenceScreen());
		}
	};

	private Preference allNotificationsPreference;
	
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
		
		addPreferencesFromResource(R.xml.preferences_notifications);
		allNotificationsPreference = findPreference("allNotifications");
		allNotificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (areNotificationsEnabled(getActivity()) == Boolean.FALSE)
					openAppNotificationSystemSettings();
				else
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
									openAppNotificationSystemSettings();
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
	}
	
	private void openAppNotificationSystemSettings() {
		Intent appNotificationSettingsIntent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS")
			.putExtra("app_package", CurrentProcess.context().getPackageName())
			.putExtra("app_uid", CurrentProcess.context().getApplicationInfo().uid);

		try {
			startActivityForResult(appNotificationSettingsIntent, REQUEST_CODE_ALL_NOTIFICATIONS_CHANGED);
		} catch (Exception ex) {
			Log.w(NotificationsFragment.class.getSimpleName(), "Couldn't directly open app notification settings. Opening app settings instead.", ex);

			Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					.setData(Uri.fromParts("package", CurrentProcess.context().getPackageName(), null));

			startActivity(appSettingsIntent);
			//TODO: This activity might not exist either. Decide what to do.
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_ALL_NOTIFICATIONS_CHANGED && areNotificationsEnabled(getActivity()) != Boolean.FALSE) {
			//The user might have just unblocked the app's notifications, so better add the status notification if needed.
			RotationServiceCommand updateNotificationCommand = new RotationServiceCommand(RotationServiceCommand.ACTION_UPDATE_NOTIFICATION);
			updateNotificationCommand.toastIfServiceDisabled = false;

			try {
				RotationServiceReceiver.getPendingIntent(updateNotificationCommand, PendingIntentIds.NOTIFICATION_SETTINGS_UPDATE_NOTIFICATION).send();
			} catch (PendingIntent.CanceledException e) {
				throw new RuntimeException(e); //Don't expect this to actually happen
			}
		}

	}
	
	/**
	 * Returns null if unknown
	 */
	public static Boolean areNotificationsEnabled(Context context) {
		if (Build.VERSION.SDK_INT >= 24)
			return SystemServices.notificationManager().areNotificationsEnabled();
		else if (Build.VERSION.SDK_INT >= 18) {
			// Before KitKat, NotificationManagerCompat.areNotificationsEnabled() is hard-coded to return true.
			// It also returns true on error.
			// However, the below code still worked on the Android 4.3 emulator.
			
			AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
			
			try {
				Method checkOpNoThrow = appOpsManager.getClass().getMethod(
					"checkOpNoThrow",
					Integer.TYPE,
					Integer.TYPE,
					String.class
				);
				
				int opPostNotification = appOpsManager.getClass().getDeclaredField("OP_POST_NOTIFICATION").getInt(null);
				int result = (int) checkOpNoThrow.invoke(appOpsManager, opPostNotification, context.getApplicationInfo().uid, context.getPackageName());
				return result == AppOpsManager.MODE_ALLOWED;
				
			} catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException | ClassCastException e) {
				Log.w(NotificationsFragment.class.getSimpleName(), "Couldn't determine whether notifications are allowed for this app.", e);
				return null; // This code is a hack, so we don't want the app to crash if it doesn't work.
			}
		}
		else if (Build.VERSION.SDK_INT >= 16) {
			return null; // Not aware of a way to do this on these Android versions.
		}
		else {
			return true; // Notifications couldn't be blocked in Android ICS.
		}
	}
	
	@SuppressLint("NewApi")
	@Override
	public void onStart() {
		super.onStart();
		
		Boolean areNotificationsEnabled = areNotificationsEnabled(getActivity());
		
		int text;
		if (areNotificationsEnabled == null)
			text = R.string.preference_all_notifications_summary;
		else
			text = areNotificationsEnabled ? R.string.on : R.string.off;
		
		allNotificationsPreference.setSummary(text);
	}
	
	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(ApplicationSettings.remote().notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
			
			getPreferenceScreen().removePreference(allNotificationsPreference);
		}
	}

}
