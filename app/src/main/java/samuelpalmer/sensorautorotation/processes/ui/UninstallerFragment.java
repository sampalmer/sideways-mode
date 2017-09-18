package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UninstallerFragment extends Fragment {
	
	void uninstall() {
		ServiceSynchroniser serviceSynchroniser = new ServiceSynchroniser(ApplicationSettings.instance());
		if (serviceSynchroniser.serviceEnabled())
			serviceSynchroniser.disableService();
		
		Intent uninstallIntent = new SystemIntents(getActivity()).uninstall();
		startActivity(uninstallIntent);
	}
	
}
