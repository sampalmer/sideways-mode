package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import samuelpalmer.common.IntentExtensions;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.common.settings.application.SettingsInitialiser;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceReceiver;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class MainSettingsFragment extends PreferenceFragment {
	
	private static final int REQUEST_CODE_ALL_NOTIFICATIONS_CHANGED = 1;
	
	private Preference forcedAppsPreference;
	private AccessibilityServiceEnabledStateMonitor accessibilityServiceEnabledStateMonitor;
	private Settings.Setting<Set<String>> forcedApps;
	
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

		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		//TODO: Apply this to the other settings fragments as well
		new SettingsInitialiser(ApplicationSettings.instance()).ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);
		
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
			removePreference(quickSettingPreference);
		
		Preference assistPreference = findPreference("assist");
		if (Build.VERSION.SDK_INT >= 23) {
			final Intent assistSettingsIntent = new Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS);
			
			if (IntentExtensions.activityDoesntExist(assistSettingsIntent, SystemServices.packageManager()))
				removePreference(assistPreference);
			else
				assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
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
									try {
										startActivity(assistSettingsIntent);
									}
									catch (ActivityNotFoundException e) {
										throw new RuntimeException("Couldn't find assist settings activity. The user shouldn't have been able to get this far.");
									}
								}
							})
							.show();
						
						return true;
					}
				});
		}
		else if (Build.VERSION.SDK_INT >= 16)
			if (hasNavigationBar() == Boolean.FALSE)
				removePreference(assistPreference);
			else
				assistPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference preference) {
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
		else
			removePreference(assistPreference);
		
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
			removePreference(smallAppPreference);
		
		//TODO: move all of this preference summary updating stuff into its own class
		forcedApps = ApplicationSettings.instance().forcedOrientationAppPackages();
		forcedAppsPreference = findPreference(forcedApps.getKey());
		accessibilityServiceEnabledStateMonitor = new AccessibilityServiceEnabledStateMonitor();
		
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
		
		notificationPriorityChanged = ApplicationSettings.instance().notificationPriority();
		notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);
	}
	
	private void openAppNotificationSystemSettings() {
		Intent appNotificationSettingsIntent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS")
			.putExtra("app_package", CurrentProcess.context().getPackageName())
			.putExtra("app_uid", CurrentProcess.context().getApplicationInfo().uid);
		
		try {
			startActivityForResult(appNotificationSettingsIntent, REQUEST_CODE_ALL_NOTIFICATIONS_CHANGED);
		} catch (Exception ex) {
			Log.w(MainSettingsFragment.class.getSimpleName(), "Couldn't directly open app notification settings. Opening app settings instead.", ex);
			
			Intent appSettingsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
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
			ApplicationInstanceCommand updateNotificationCommand = new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_UPDATE_NOTIFICATION);
			updateNotificationCommand.toastIfServiceDisabled = false;
			
			try {
				ApplicationInstanceReceiver.getPendingIntent(updateNotificationCommand, PendingIntentIds.NOTIFICATION_SETTINGS_UPDATE_NOTIFICATION).send();
			} catch (PendingIntent.CanceledException e) {
				throw new RuntimeException(e); //Don't expect this to actually happen
			}
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
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
				Log.w(MainSettingsFragment.class.getSimpleName(), "Couldn't determine whether notifications are allowed for this app.", e);
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
	
	private void removePreference(Preference preference) {
		PreferenceGroup parent = getParent(getPreferenceScreen(), preference);
		if (parent == null)
			throw new RuntimeException("Couldn't find preference");
		
		parent.removePreference(preference);
	}
	
	private PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
		for (int i = 0; i < groupToSearchIn.getPreferenceCount(); ++i) {
			Preference child = groupToSearchIn.getPreference(i);
			
			if (child == preference)
				return groupToSearchIn;
			
			if (child instanceof PreferenceGroup) {
				PreferenceGroup childGroup = (PreferenceGroup)child;
				PreferenceGroup result = getParent(childGroup, preference);
				if (result != null)
					return result;
			}
		}
		
		return null;
	}
	
	private static boolean supportsSmallApp() {
		try {
			Class.forName("com.sony.smallapp.SmallApplication");
		} catch (ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns {@code null} if this couldn't be determined.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static Boolean hasNavigationBar() {
		try {
			Class<?> serviceManager = Class.forName("android.os.ServiceManager");
			IBinder serviceBinder = (IBinder)serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
			Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
			Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
			Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
			return (boolean)hasNavigationBar.invoke(windowManagerService);
		} catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.w(MainSettingsFragment.class.getSimpleName(), "Couldn't determine whether the device has a navigation bar", e);
			return null;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		accessibilityServiceEnabledStateMonitor.subscribe(forcedAppsChanged);
		forcedApps.subscribe(forcedAppsChanged);
		updateForcedApps();
		
		Boolean areNotificationsEnabled = areNotificationsEnabled(getActivity());
		
		int text;
		if (areNotificationsEnabled == null)
			text = R.string.preference_all_notifications_summary;
		else
			text = areNotificationsEnabled ? R.string.on : R.string.off;
		
		allNotificationsPreference.setSummary(text);
	}

	@Override
	public void onStop() {
		super.onStop();

		forcedApps.unsubscribe(forcedAppsChanged);
		accessibilityServiceEnabledStateMonitor.unsubscribe(forcedAppsChanged);
	}
	
	private final EventHandler forcedAppsChanged = new EventHandler() {
		@Override
		public void update() {
			updateForcedApps();
		}
	};

	private void updateForcedApps() {
		final int quantity = forcedApps.get().size();

		if (quantity > 0 && !(accessibilityServiceEnabledStateMonitor.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(getActivity())))
			forcedAppsPreference.setSummary(R.string.permission_needed_summary);
		else {
			final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
			forcedAppsPreference.setSummary(quantityString);
		}
	}
	
	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(ApplicationSettings.instance().notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
			
			getPreferenceScreen().removePreference(allNotificationsPreference);
		}
	}
	
}
