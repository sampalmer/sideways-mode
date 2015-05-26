package samuelpalmer.sensorautorotation.components;

import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.Arrays;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.SettingsInitialiser;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

	public static class SettingsFragment extends PreferenceFragment {

		private final EventHandler<Void> enabledUpdater = new EventHandler<Void>() {
			@Override
			public void update(Void event) {
				refresh();
			}
		};

		private final EventHandler<Void> listPreferenceSummaryUpdater = new EventHandler<Void>() {
			@Override
			public void update(Void event) {
				updateListPreferenceSummaries();
			}
		};

		private void updateListPreferenceSummaries() {
			for (ApplicationSettings.Setting<?> setting : new ApplicationSettings.Setting[]{ settings.screenOffBehaviour(), settings.notificationPriority() }) {
				ListPreference preference = (ListPreference) findPreference(setting.getKey());
				preference.setSummary(preference.getEntry());
			}
		}

		private ApplicationSettings settings;

		private EventRegistrar<Void> enabledChanged;
		private EventRegistrar<Void> onScreenOffChanged;
		private EventRegistrar<Void> notificationPriorityChanged;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			settings = new ApplicationSettings();

			//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
			new SettingsInitialiser(settings).ensureInitialised();

			addPreferencesFromResource(R.xml.preferences);

			updateListPreferenceSummaries();
			hideUnsupportedOptions();

			enabledChanged = settings.enabled().onChange();
			onScreenOffChanged = settings.screenOffBehaviour().onChange();
			notificationPriorityChanged = settings.notificationPriority().onChange();
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
		public void onResume() {
			super.onResume();
			enabledChanged.subscribe(enabledUpdater);
			onScreenOffChanged.subscribe(listPreferenceSummaryUpdater);
			notificationPriorityChanged.subscribe(listPreferenceSummaryUpdater);

			refresh();
		}

		private void refresh() {
			CheckBoxPreference preference = (CheckBoxPreference) findPreference(settings.enabled().getKey());
			preference.setChecked(settings.enabled().get());
		}

		@Override
		public void onPause() {
			super.onPause();
			notificationPriorityChanged.unsubscribe(listPreferenceSummaryUpdater);
			onScreenOffChanged.unsubscribe(listPreferenceSummaryUpdater);
			enabledChanged.unsubscribe(enabledUpdater);
		}

	}
}
