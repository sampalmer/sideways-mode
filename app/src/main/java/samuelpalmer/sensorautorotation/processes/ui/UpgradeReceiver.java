package samuelpalmer.sensorautorotation.processes.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver ensure the UI process will be created when the app is upgraded.
 * This allows the process to generate the upgrade notification straight away.
 */
public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(UpgradeReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
		
		// Not doing anything here; the upgrade logic will be triggered in Application.onCreate()
		
		if (!Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction()))
			Log.w(UpgradeReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
	}

}
