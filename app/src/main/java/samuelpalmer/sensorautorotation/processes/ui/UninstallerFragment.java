package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UninstallerFragment extends Fragment {
	
	void uninstall() {
		if (ServiceSynchroniser.serviceEnabled())
			ServiceSynchroniser.disableService();
		
		Intent uninstallIntent = new SystemIntents(getActivity()).uninstall();
		startActivity(uninstallIntent);
	}
	
}
