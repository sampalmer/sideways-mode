package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceClient;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;

public class UninstallerFragment extends Fragment {
	
	public void uninstall() {
		ApplicationInstanceClient.send(new ApplicationInstanceCommand(ApplicationInstanceCommand.ACTION_TURN_OFF));
		
		Intent uninstallIntent = new SystemIntents(getActivity()).uninstall();
		startActivity(uninstallIntent);
	}
	
}
