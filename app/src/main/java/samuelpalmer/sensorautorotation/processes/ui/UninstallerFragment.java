package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UninstallerFragment extends Fragment {
	
	private static final int REQUEST_CODE_UNINSTALL = 1;
	
	private ServiceSynchroniser serviceSynchroniser;
	private boolean appWasEnabled;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		serviceSynchroniser = new ServiceSynchroniser(ApplicationSettings.instance());
		
		if (savedInstanceState != null)
			appWasEnabled = savedInstanceState.getBoolean(null);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(null, appWasEnabled);
	}
	
	void uninstall() {
		appWasEnabled = serviceSynchroniser.serviceEnabled();
		if (appWasEnabled)
			serviceSynchroniser.disableService();
		
		Uri packageUri = Uri.fromParts("package", getActivity().getPackageName(), null);
		Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
		startActivityForResult(uninstallIntent, REQUEST_CODE_UNINSTALL);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// If we got back here, the app didn't get uninstalled
		if (requestCode == REQUEST_CODE_UNINSTALL && appWasEnabled)
			serviceSynchroniser.enableService();
	}
}
