package samuelpalmer.sensorautorotation.components;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}

	public static class SettingsFragment extends PreferenceFragment {

		private final EventHandler<Void> serviceUpdater = new EventHandler<Void>() {
			@Override
			public void update(Void event) {
				new ServiceController(getActivity()).updateFromSettings();
			}
		};

		private EventRegistrar<Void> enabledChanged;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

			enabledChanged = new Settings(getActivity()).enabled().onChange();
		}

		@Override
		public void onResume() {
			super.onResume();
			enabledChanged.subscribe(serviceUpdater);
		}

		@Override
		public void onPause() {
			super.onPause();
			enabledChanged.unsubscribe(serviceUpdater);
		}

	}
}
