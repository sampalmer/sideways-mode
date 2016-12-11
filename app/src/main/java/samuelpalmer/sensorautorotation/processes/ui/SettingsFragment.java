package samuelpalmer.sensorautorotation.processes.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.settings.application.SettingsInitialiser;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class SettingsFragment extends PreferenceFragment {

	private static final int PERMISSION_REQUEST_CODE = 1;
	private Preference forcedAppsPreference;
	private AccessibilityServiceEnabledStateMonitor accessibilityServiceEnabledStateMonitor;
	private samuelpalmer.common.settings.application.Settings.Setting<Set<String>> forcedApps;

	private TwoStatePreference enabledControl() {
		return (TwoStatePreference) findPreference(ApplicationSettings.remote().enabled().getKey());
	}

	private final EventHandler enabledUpdater = new EventHandler() {
		@Override
		public void update() {
			TwoStatePreference preference = enabledControl();
			preference.setChecked(ApplicationSettings.remote().enabled().get());
		}
	};

	private final EventHandler listPreferenceSummaryUpdater = new EventHandler() {
		@Override
		public void update() {
			updateListPreferenceSummaries(getPreferenceScreen());
		}
	};

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

	private EventRegistrar enabledChanged;
	private EventRegistrar notificationPriorityChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		new SettingsInitialiser(ApplicationSettings.remote()).ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);

		updateListPreferenceSummaries(getPreferenceScreen());
		hideUnsupportedOptions();

		enabledChanged = ApplicationSettings.remote().enabled();
		notificationPriorityChanged = ApplicationSettings.remote().notificationPriority();

		//TODO: Move subscription into onStart
		enabledChanged.subscribe(enabledUpdater);
		notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);

		enabledControl().setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (VERSION.SDK_INT >= 23 && (boolean) newValue && !Settings.System.canWrite(getContext())) {
					requestSystemSettingsPermission();
					return false;
				} else {
					//We want to update the service state _after_ the shared preferences has been set
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							new ServiceSynchroniser(ApplicationSettings.remote()).updateFromSettings();
						}
					});

					return true;
				}
			}
		});

		forcedApps = ApplicationSettings.remote().forcedOrientationAppPackages();
		forcedAppsPreference = findPreference(forcedApps.getKey());
		accessibilityServiceEnabledStateMonitor = new AccessibilityServiceEnabledStateMonitor();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
		enabledChanged.unsubscribe(enabledUpdater);
	}

	@Override
	public void onStart() {
		super.onStart();
		accessibilityServiceEnabledStateMonitor.subscribe(forcedAppsChanged);
		forcedApps.subscribe(forcedAppsChanged);
		updateForcedApps();
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
			forcedAppsPreference.setSummary(R.string.preference_forced_apps_summary_disabled);
		else {
			final String quantityString = getResources().getQuantityString(R.plurals.preference_forced_apps_summary_enabled, quantity, quantity);
			forcedAppsPreference.setSummary(quantityString);
		}
	}

	@TargetApi(VERSION_CODES.M)
	private void requestSystemSettingsPermission() {
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.environment_error_permission_needed_summary)
			.setMessage(R.string.environment_error_permission_needed_system_settings_message)
			.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//TODO: Handle case where this activity isn't available
					startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getContext().getPackageName(), null)), PERMISSION_REQUEST_CODE);
				}
			})
			.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (VERSION.SDK_INT >= 23 && requestCode == PERMISSION_REQUEST_CODE)
			handleSystemSettingsRequestResult();
	}

	@TargetApi(VERSION_CODES.M)
	private void handleSystemSettingsRequestResult() {
		if (Settings.System.canWrite(getContext()))
			new ServiceSynchroniser(ApplicationSettings.remote()).enableService();
		else
			Toast
				.makeText(getContext(), R.string.warning_permission_not_granted_system_settings, Toast.LENGTH_LONG)
				.show();
	}

	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(ApplicationSettings.remote().notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
		}
	}

}
