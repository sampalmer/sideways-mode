package samuelpalmer.sensorautorotation.processes.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;

/**
 * Provides a way to communicate with RotationService using broadcasts.
 */
public class RotationServiceReceiver extends BroadcastReceiver {

	public static PendingIntent getPendingIntent(String action) {
		ComponentType componentType = ComponentType.create(RotationServiceReceiver.class);
		return componentType.pendingIntent(componentType.intent().setAction(action));
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		RotationServiceClient.send(intent.getAction());
	}

}
