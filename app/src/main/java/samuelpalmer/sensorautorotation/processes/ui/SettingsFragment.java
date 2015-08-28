package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Arrays;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.SettingsInitialiser;

public class SettingsFragment extends PreferenceFragment {

	public static final int PERMISSION_REQUEST_CODE = 1;

	private CheckBoxPreference enabledCheckbox() {
		return (CheckBoxPreference) findPreference(ApplicationSettings.instance().enabled().getKey());
	}

	private final EventHandler enabledUpdater = new EventHandler() {
		@Override
		public void update() {
			CheckBoxPreference preference = enabledCheckbox();
			preference.setChecked(ApplicationSettings.instance().enabled().get());
		}
	};

	private final EventHandler listPreferenceSummaryUpdater = new EventHandler() {
		@Override
		public void update() {
			updateListPreferenceSummaries();
		}
	};

	private void updateListPreferenceSummaries() {
		PreferenceScreen screen = getPreferenceScreen();
		for (int i = 0; i < screen.getPreferenceCount(); ++i) {
			Preference preference = screen.getPreference(i);
			if (preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				listPreference.setSummary(listPreference.getEntry());
			}
		}
	}

	private EventRegistrar enabledChanged;
	private EventRegistrar notificationPriorityChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		new SettingsInitialiser().ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);

		updateListPreferenceSummaries();
		hideUnsupportedOptions();

		enabledChanged = ApplicationSettings.instance().enabled();
		notificationPriorityChanged = ApplicationSettings.instance().notificationPriority();

		enabledChanged.subscribe(enabledUpdater);
		notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);

		enabledCheckbox().setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (
					VERSION.SDK_INT >= 23
					&&
					(boolean) newValue
					&&
					!Settings.System.canWrite(getContext())
				) {
					new AlertDialog.Builder(getContext())
							.setTitle(R.string.permission_needed_summary)
							.setMessage(R.string.permission_needed_message)
							.setNegativeButton(android.R.string.cancel, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {}
							})
							.setPositiveButton(android.R.string.ok, new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getContext().getPackageName(), null)), PERMISSION_REQUEST_CODE);
								}
							})
							.show();

					return false;
				} else {
					//We want to update the service state _after_ the shared preferences has been set
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							ServiceController.updateFromSettings();
						}
					});

					return true;
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (Settings.System.canWrite(getContext()))
			ServiceController.enableService();
		else
			Toast
					.makeText(getContext(), R.string.warning_wasn_enabled_permission_denied, Toast.LENGTH_LONG)
					.show();
	}

	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(ApplicationSettings.instance().notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
		enabledChanged.unsubscribe(enabledUpdater);
	}

}
