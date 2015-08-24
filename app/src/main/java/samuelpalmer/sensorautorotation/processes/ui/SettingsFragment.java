package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.Arrays;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.SettingsInitialiser;

public class SettingsFragment extends PreferenceFragment {

	private final EventHandler enabledUpdater = new EventHandler() {
		@Override
		public void update() {
			refresh();
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
	public void onStart() {
		super.onStart();

		enabledChanged.subscribe(enabledUpdater);
		notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);

		refresh();
	}

	@Override
	public void onStop() {
		super.onStop();

		notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
		enabledChanged.unsubscribe(enabledUpdater);
	}

	private void refresh() {
		CheckBoxPreference preference = (CheckBoxPreference) findPreference(ApplicationSettings.instance().enabled().getKey());
		preference.setChecked(ApplicationSettings.instance().enabled().get());
	}
}
