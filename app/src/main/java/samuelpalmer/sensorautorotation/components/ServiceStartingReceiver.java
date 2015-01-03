package samuelpalmer.sensorautorotation.components;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;

public class ServiceStartingReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final ServiceController service = new ServiceController(context);
		if (new Settings(context).enabled().get())
			service.ensureStarted();
	}

}
