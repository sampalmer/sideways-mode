package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class FirstRunLauncherFragment extends Fragment {

	private static final int REQUEST_CODE_INSTRUCT_USER = 1;

	private ApplicationSettings.Setting<Integer> instructedVersionPreference;
	private boolean recordUserInstructed;

	public void onPreferencesAvailable(SharedPreferences prefs) {
		instructedVersionPreference = new ApplicationSettings(prefs).lastVersionUserInstructedAbout();

		recordUserInstructedIfNecessary();
		instructUserIfNecessary();
	}

	@Override
	public void onResume() {
		super.onResume();
		instructUserIfNecessary();
	}

	private void instructUserIfNecessary() {
		if (isResumed() && instructedVersionPreference != null && !instructedVersionPreference.hasValue())
			startActivityForResult(ComponentType.create(FirstRunActivity.class).makeIntent(), REQUEST_CODE_INSTRUCT_USER);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_INSTRUCT_USER && resultCode == Activity.RESULT_OK) {
			recordUserInstructed = true;
			recordUserInstructedIfNecessary();
		}
	}

	private void recordUserInstructedIfNecessary() {
		if (instructedVersionPreference != null && recordUserInstructed) {
			instructedVersionPreference.setAndCommit(VersionInfo.installedVersionCode());
			recordUserInstructed = false;
		}
	}

}
