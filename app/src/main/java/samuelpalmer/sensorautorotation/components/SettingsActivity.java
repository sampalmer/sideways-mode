package samuelpalmer.sensorautorotation.components;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
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
				new ServiceController(getActivity()).updateFromSettings();
				refresh();
			}
		};

		private final EventHandler<Void> onScreenOnSummaryUpdater = new EventHandler<Void>() {
			@Override
			public void update(Void event) {
				updateOnScreenOnSummary();
			}
		};

		private void updateOnScreenOnSummary() {
			ListPreference preference = (ListPreference)findPreference(settings.screenOnBehaviour().getKey());
			preference.setSummary(preference.getEntry());
		}

		private ApplicationSettings settings;
		private EventRegistrar<Void> enabledChanged;
		private EventRegistrar<Void> onScreenOnChanged;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			settings = new ApplicationSettings(getActivity());
			new SettingsInitialiser(settings).ensureInitialised();

			addPreferencesFromResource(R.xml.preferences);

			updateOnScreenOnSummary();

			enabledChanged = settings.enabled().onChange();
			onScreenOnChanged = settings.screenOnBehaviour().onChange();
		}

		@Override
		public void onResume() {
			super.onResume();
			enabledChanged.subscribe(enabledUpdater);
			onScreenOnChanged.subscribe(onScreenOnSummaryUpdater);

			refresh();
		}

		private void refresh() {
			CheckBoxPreference preference = (CheckBoxPreference) findPreference(settings.enabled().getKey());
			preference.setChecked(settings.enabled().get());
		}

		@Override
		public void onPause() {
			super.onPause();
			onScreenOnChanged.unsubscribe(onScreenOnSummaryUpdater);
			enabledChanged.unsubscribe(enabledUpdater);
		}

	}
}
