package samuelpalmer.sensorautorotation.components;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

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

		private final EventHandler<Void> onScreenOffSummaryUpdater = new EventHandler<Void>() {
			@Override
			public void update(Void event) {
				updateOnScreenOffSummary();
			}
		};

		private void updateOnScreenOffSummary() {
			ListPreference preference = (ListPreference)findPreference(settings.screenOffBehaviour().getKey());
			preference.setSummary(preference.getEntry());
		}

		private ApplicationSettings settings;
		private EventRegistrar<Void> enabledChanged;
		private EventRegistrar<Void> onScreenOffChanged;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			settings = new ApplicationSettings(getActivity());

			//We're not using the built-in way of specifying default preference values, so they must be explicitly set in order to show correctly in the settings UI
			new SettingsInitialiser(settings).ensureInitialised();

			addPreferencesFromResource(R.xml.preferences);

			updateOnScreenOffSummary();

			enabledChanged = settings.enabled().onChange();
			onScreenOffChanged = settings.screenOffBehaviour().onChange();
		}

		@Override
		public void onResume() {
			super.onResume();
			enabledChanged.subscribe(enabledUpdater);
			onScreenOffChanged.subscribe(onScreenOffSummaryUpdater);

			refresh();
		}

		private void refresh() {
			CheckBoxPreference preference = (CheckBoxPreference) findPreference(settings.enabled().getKey());
			preference.setChecked(settings.enabled().get());
		}

		@Override
		public void onPause() {
			super.onPause();
			onScreenOffChanged.unsubscribe(onScreenOffSummaryUpdater);
			enabledChanged.unsubscribe(enabledUpdater);
		}

	}
}
