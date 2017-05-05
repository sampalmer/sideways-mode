package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Fragment;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class FirstRunLauncherFragment extends Fragment {
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (!ApplicationSettings.remote().lastVersionUserInstructedAbout().hasValue()) {
			Intent intent = ComponentType.create(InstructionsActivity.class)
				.intent()
				.putExtra(InstructionsActivity.EXTRA_FIRST_RUN, true);
			
			startActivity(intent);
			getActivity().finish();
		}
	}
	
}
