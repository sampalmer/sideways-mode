package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class FirstRunLauncherFragment extends Fragment {

	private static final int REQUEST_CODE_INSTRUCT_USER = 1;

	@Override
	public void onResume() {
		super.onResume();
		instructUserIfNecessary();
	}

	private void instructUserIfNecessary() {
		if (!instructedVersionPreference().hasValue())
			startActivityForResult(ComponentType.create(InstructionsActivity.class).intent(), REQUEST_CODE_INSTRUCT_USER);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_INSTRUCT_USER && resultCode == Activity.RESULT_OK)
			instructedVersionPreference().set(VersionInfo.installedVersionCode());
	}

	private ApplicationSettings.Setting<Integer> instructedVersionPreference() {
		return ApplicationSettings.remote().lastVersionUserInstructedAbout();
	}

}
