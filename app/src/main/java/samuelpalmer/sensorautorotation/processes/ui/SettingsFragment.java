package samuelpalmer.sensorautorotation.processes.ui;

import android.content.SharedPreferences;
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

	private boolean created;
	private SharedPreferences preferences;
	private boolean started;
	private boolean initialised;
	private boolean listening;

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

	private ApplicationSettings settings;
	private EventRegistrar enabledChanged;
	private EventRegistrar onScreenOffChanged;
	private EventRegistrar notificationPriorityChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		created = true;
		initialiseIfReady();
	}

	public void onPreferencesAvailable(SharedPreferences preferences) {
		this.preferences = preferences;
		initialiseIfReady();
		listenIfReady();
	}

	private void initialiseIfReady() {
		if (created && preferences != null)
			initialise();
	}

	private void initialise() {
		settings = new ApplicationSettings(preferences);

		//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
		new SettingsInitialiser(settings).ensureInitialised();

		addPreferencesFromResource(R.xml.preferences);

		updateListPreferenceSummaries();
		hideUnsupportedOptions();

		enabledChanged = settings.enabled().onChange();
		onScreenOffChanged = settings.screenOffBehaviour().onChange();
		notificationPriorityChanged = settings.notificationPriority().onChange();

		initialised = true;
	}

	private void hideUnsupportedOptions() {
		if (Build.VERSION.SDK_INT < 16) {
			ListPreference notificationPriorityPreference = (ListPreference) findPreference(settings.notificationPriority().getKey());
			CharSequence[] originalEntries = notificationPriorityPreference.getEntries();
			int newLength = originalEntries.length - 1;
			notificationPriorityPreference.setEntries(Arrays.copyOfRange(originalEntries, 0, newLength));
			notificationPriorityPreference.setEntryValues(Arrays.copyOfRange(notificationPriorityPreference.getEntryValues(), 0, newLength));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		started = true;
		listenIfReady();
	}

	private void listenIfReady() {
		if (initialised && started) {
			enabledChanged.subscribe(enabledUpdater);
			onScreenOffChanged.subscribe(listPreferenceSummaryUpdater);
			notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);

			listening = true;

			refresh();
		}
	}

	private void refresh() {
		CheckBoxPreference preference = (CheckBoxPreference) findPreference(settings.enabled().getKey());
		preference.setChecked(settings.enabled().get());
	}

	@Override
	public void onStop() {
		super.onStop();
		started = false;

		if (initialised && listening) {
			notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
			onScreenOffChanged.unsubscribe(listPreferenceSummaryUpdater);
			enabledChanged.unsubscribe(enabledUpdater);

			listening = false;
		}
	}
}
