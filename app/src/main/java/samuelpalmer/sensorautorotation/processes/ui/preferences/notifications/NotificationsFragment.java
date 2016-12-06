package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import java.util.Arrays;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;


public class NotificationsFragment extends PreferenceFragment {

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

	private EventRegistrar notificationPriorityChanged;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences_notifications);
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
