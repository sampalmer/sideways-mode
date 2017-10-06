package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.ApplicationCommand;

public class UninstallerFragment extends Fragment {
	
	public void uninstall() {
		new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(getContext());
		
		Intent uninstallIntent = new SystemIntents(getActivity()).uninstall();
		startActivity(uninstallIntent);
	}
	
}
