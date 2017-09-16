package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UninstallerFragment extends Fragment {
	
	void uninstall() {
		ServiceSynchroniser serviceSynchroniser = new ServiceSynchroniser(ApplicationSettings.instance());
		if (serviceSynchroniser.serviceEnabled())
			serviceSynchroniser.disableService();
		
		Uri packageUri = Uri.fromParts("package", getActivity().getPackageName(), null);
		Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		startActivity(uninstallIntent);
	}
	
}
